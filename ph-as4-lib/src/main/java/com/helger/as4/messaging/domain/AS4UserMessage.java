/**
 * Copyright (C) 2015-2018 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.as4.messaging.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.as4.soap.ESOAPVersion;
import com.helger.as4lib.ebms3header.Ebms3CollaborationInfo;
import com.helger.as4lib.ebms3header.Ebms3MessageInfo;
import com.helger.as4lib.ebms3header.Ebms3MessageProperties;
import com.helger.as4lib.ebms3header.Ebms3PartyInfo;
import com.helger.as4lib.ebms3header.Ebms3PayloadInfo;
import com.helger.as4lib.ebms3header.Ebms3UserMessage;
import com.helger.commons.ValueEnforcer;

/**
 * AS4 user message
 *
 * @author Philip Helger
 */
public class AS4UserMessage extends AbstractAS4Message <AS4UserMessage>
{
  private final Ebms3UserMessage m_aUserMessage;

  public AS4UserMessage (@Nonnull final ESOAPVersion eSOAPVersion, @Nonnull final Ebms3UserMessage aUserMessage)
  {
    super (eSOAPVersion, EAS4MessageType.USER_MESSAGE);

    ValueEnforcer.notNull (aUserMessage, "UserMessage");
    m_aMessaging.addUserMessage (aUserMessage);

    m_aUserMessage = aUserMessage;
  }

  /**
   * @return The {@link Ebms3UserMessage} passed in the constructor. Never
   *         <code>null</code>.
   */
  @Nonnull
  public final Ebms3UserMessage getEbms3UserMessage ()
  {
    return m_aUserMessage;
  }

  @Nonnull
  public static AS4UserMessage create (@Nonnull final ESOAPVersion eSOAPVersion,
                                       @Nonnull final Ebms3UserMessage aUserMessage)
  {
    return new AS4UserMessage (eSOAPVersion, aUserMessage);
  }

  @Nonnull
  public static AS4UserMessage create (@Nonnull final Ebms3MessageInfo aEbms3MessageInfo,
                                       @Nullable final Ebms3PayloadInfo aEbms3PayloadInfo,
                                       @Nonnull final Ebms3CollaborationInfo aEbms3CollaborationInfo,
                                       @Nonnull final Ebms3PartyInfo aEbms3PartyInfo,
                                       @Nullable final Ebms3MessageProperties aEbms3MessageProperties,
                                       @Nonnull final ESOAPVersion eSOAPVersion)
  {
    final Ebms3UserMessage aUserMessage = new Ebms3UserMessage ();

    // Party Information
    aUserMessage.setPartyInfo (aEbms3PartyInfo);

    // Collaboration Information
    aUserMessage.setCollaborationInfo (aEbms3CollaborationInfo);

    // Properties
    aUserMessage.setMessageProperties (aEbms3MessageProperties);

    // Payload Information
    aUserMessage.setPayloadInfo (aEbms3PayloadInfo);

    // Message Info
    aUserMessage.setMessageInfo (aEbms3MessageInfo);

    return new AS4UserMessage (eSOAPVersion, aUserMessage);
  }
}
