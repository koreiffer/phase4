/*
 * Copyright (C) 2015-2023 Philip Helger (www.helger.com)
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
package com.helger.phase4.crypto;

import java.security.Provider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Default implementation of {@link IAS4IncomingSecurityConfiguration}.
 *
 * @author Philip Helger
 * @since 2.1.3
 */
@NotThreadSafe
public class AS4IncomingSecurityConfiguration implements IAS4IncomingSecurityConfiguration
{
  private Provider m_aSecurityProvider;

  public AS4IncomingSecurityConfiguration ()
  {}

  @Nullable
  public Provider getSecurityProvider ()
  {
    return m_aSecurityProvider;
  }

  @Nonnull
  public AS4IncomingSecurityConfiguration getSecurityProvider (@Nullable final Provider a)
  {
    m_aSecurityProvider = a;
    return this;
  }

  @Nonnull
  public static IAS4IncomingSecurityConfiguration createDefaultInstance ()
  {
    return new AS4IncomingSecurityConfiguration ();
  }
}
