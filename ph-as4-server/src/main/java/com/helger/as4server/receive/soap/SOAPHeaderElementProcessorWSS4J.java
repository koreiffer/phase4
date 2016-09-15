package com.helger.as4server.receive.soap;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.apache.wss4j.common.ext.Attachment;
import org.apache.wss4j.common.util.AttachmentUtils;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.as4lib.attachment.AttachmentCallbackHandler;
import com.helger.as4lib.constants.CAS4;
import com.helger.as4lib.crypto.AS4CryptoFactory;
import com.helger.as4lib.crypto.ECryptoAlgorithmSign;
import com.helger.as4lib.crypto.ECryptoAlgorithmSignDigest;
import com.helger.as4lib.ebms3header.Ebms3UserMessage;
import com.helger.as4lib.error.EEbmsError;
import com.helger.as4lib.model.pmode.PModeLeg;
import com.helger.as4lib.wss.EWSSVersion;
import com.helger.as4server.receive.AS4MessageState;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.state.ESuccess;
import com.helger.xml.XMLHelper;
import com.helger.xml.serialize.write.XMLWriter;

public class SOAPHeaderElementProcessorWSS4J implements ISOAPHeaderElementProcessor
{
  private static final Logger LOG = LoggerFactory.getLogger (SOAPHeaderElementProcessorWSS4J.class);

  @Nonnull
  public ESuccess processHeaderElement (@Nonnull final Document aSOAPDoc,
                                        @Nonnull final Element aSecurityNode,
                                        @Nonnull final ICommonsList <Attachment> aAttachments,
                                        @Nonnull final AS4MessageState aState,
                                        @Nonnull final ErrorList aErrorList)
  {

    final PModeLeg aPModeLeg1 = aState.getPMode ().getLeg1 ();
    // Does security - legpart checks if not <code>null</code>
    if (aPModeLeg1.getSecurity () != null)
    {
      // TODO set to debug
      LOG.info (XMLWriter.getXMLString (aSecurityNode));

      // Get Signature Algorithm
      Element aSignedNode = XMLHelper.getFirstChildElementOfName (aSecurityNode, CAS4.DS_NS, "Signature");
      if (aSignedNode != null)
      {
        aSignedNode = XMLHelper.getFirstChildElementOfName (aSignedNode, CAS4.DS_NS, "SignedInfo");
        final Element aSignatureAlgorithm = XMLHelper.getFirstChildElementOfName (aSignedNode,
                                                                                  CAS4.DS_NS,
                                                                                  "SignatureMethod");
        String sAlgorithm = aSignatureAlgorithm == null ? null : aSignatureAlgorithm.getAttribute ("Algorithm");
        if (ECryptoAlgorithmSign.getFromURIOrNull (sAlgorithm) == null)
        {
          aErrorList.add (SingleError.builderError ()
                                     .setErrorText ("Error processing the Security Header, your signing algorithm '" +
                                                    sAlgorithm +
                                                    "' is incorrect. Expected one of the following '" +
                                                    Arrays.asList (ECryptoAlgorithmSign.values ()) +
                                                    "' algorithms")
                                     .build ());
          return ESuccess.FAILURE;
        }

        // Get Signature Digest Algorithm
        aSignedNode = XMLHelper.getFirstChildElementOfName (aSignedNode, CAS4.DS_NS, "Reference");
        aSignedNode = XMLHelper.getFirstChildElementOfName (aSignedNode, CAS4.DS_NS, "DigestMethod");
        sAlgorithm = aSignedNode == null ? null : aSignedNode.getAttribute ("Algorithm");
        if (ECryptoAlgorithmSignDigest.getFromURIOrNull (sAlgorithm) == null)
        {
          aErrorList.add (SingleError.builderError ()
                                     .setErrorText ("Error processing the Security Header, your signing digest algorithm is incorrect. Expected one of the following'" +
                                                    Arrays.asList (ECryptoAlgorithmSignDigest.values ()) +
                                                    "' algorithms")
                                     .build ());
          return ESuccess.FAILURE;
        }
      }

      // Encrypted header TODO need to check BodyPayload for right or wrong
      // Algorithm
      final Element aEncryptedNode = XMLHelper.getFirstChildElementOfName (aSecurityNode, CAS4.XENC_NS, "EncryptedKey");
      if (aEncryptedNode != null)
      {
        // Encrypted checks
        LOG.info ("encrypted checks");

      }

      // Checks the WSSVersion
      if (EWSSVersion.getFromVersionOrNull (aPModeLeg1.getSecurity ().getWSSVersion ()) == null)
      {
        aErrorList.add (SingleError.builderError ()
                                   .setErrorText ("Error processing the PMode, the WSS - Version," +
                                                  aPModeLeg1.getSecurity ().getWSSVersion () +
                                                  " is incorrect")
                                   .build ());
        return ESuccess.FAILURE;
      }

      final Ebms3UserMessage aUserMessage = aState.getMessaging ().getUserMessage ().get (0);
      // Check if Attachment IDs are the same
      for (int i = 0; i < aAttachments.size (); i++)
      {
        if (!aUserMessage.getPayloadInfo ()
                         .getPartInfoAtIndex (i)
                         .getHref ()
                         .equals (aAttachments.get (i).getHeaders ().get (AttachmentUtils.MIME_HEADER_CONTENT_ID)))
        {
          // TODO change Local to dynamic one
          LOG.info ("Error processing the attachment '" +
                    aAttachments.get (i).getId () +
                    "' is not valid with what is specified in the EBMS UserMessage.");
          aErrorList.add (EEbmsError.EBMS_VALUE_INCONSISTENT.getAsError (Locale.US));

          return ESuccess.FAILURE;
        }
      }

      // Signing Check and Decryption
      final WSSecurityEngine aSecurityEngine = new WSSecurityEngine ();

      List <WSSecurityEngineResult> aResults = null;

      try
      {
        // Convert to WSS4J attachments
        final KeyStoreCallbackHandler aKeyStoreCallback = new KeyStoreCallbackHandler ();
        final AttachmentCallbackHandler aAttachmentCallbackHandler = new AttachmentCallbackHandler (aAttachments);

        final RequestData aRequestData = new RequestData ();
        aRequestData.setCallbackHandler (aKeyStoreCallback);
        if (aAttachments.isNotEmpty ())
          aRequestData.setAttachmentCallbackHandler (aAttachmentCallbackHandler);
        aRequestData.setSigVerCrypto (AS4CryptoFactory.createCrypto ());
        aRequestData.setDecCrypto (AS4CryptoFactory.createCrypto ());

        // Upon success, the SOAP document contains the decrypted content
        // afterwards!
        aResults = aSecurityEngine.processSecurityHeader (aSOAPDoc, aRequestData).getResults ();

        // TODO maybe not needed since you cant check Digest algorithm OR
        // encrypt algorithm
        aResults.forEach (x -> x.forEach ( (k, v) -> LOG.info ("KeyValuePair: " + k + "=" + v)));

        aState.setDecryptedSOAPDocument (aSOAPDoc);

        // TODO save DOc somewhere? or what should happen with it
        // aSecurityEngine.processSecurityHeader (aSOAPDoc, null,
        // aKeyStoreCallback, AS4CryptoFactory.createCrypto ());
        // System.out.println ("Decryption Result ");
        // System.out.println (XMLUtils.prettyDocumentToString (aSOAPDoc));
      }
      catch (final Exception ex)
      {
        // Decryption or Signature check failed

        LOG.info ("Error processing the WSSSecurity Header", ex);

        // TODO change Local to dynamic one + we need a way to distinct
        // signature and decrypt WSSecurityException provides no such thing
        aErrorList.add (EEbmsError.EBMS_FAILED_AUTHENTICATION.getAsError (Locale.US));

        return ESuccess.FAILURE;
      }
    }
    return ESuccess.SUCCESS;
  }
}
