/**
 * Copyright (C) 2015-2019 Philip Helger (www.helger.com)
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
package com.helger.phase4.mgr;

import javax.annotation.Nonnull;

import com.helger.phase4.duplicate.AS4DuplicateManager;
import com.helger.phase4.duplicate.IAS4DuplicateManager;
import com.helger.phase4.model.mpc.IMPCManager;
import com.helger.phase4.model.mpc.MPCManager;
import com.helger.phase4.model.pmode.IPModeManager;
import com.helger.phase4.model.pmode.PModeManager;

/**
 * Implementation of {@link IManagerFactory} creating managers that are
 * persisting to disk.
 *
 * @author Philip Helger
 * @since 0.9.6
 */
public class ManagerFactoryPersistingFileSystem implements IManagerFactory
{
  private static final String MPC_XML = "as4-mpc.xml";
  private static final String PMODE_XML = "as4-pmode.xml";
  private static final String INCOMING_DUPLICATE_XML = "as4-duplicate-incoming.xml";

  @Nonnull
  public IMPCManager createMPCManager () throws Exception
  {
    return new MPCManager (MPC_XML);
  }

  @Nonnull
  public IPModeManager createPModeManager () throws Exception
  {
    return new PModeManager (PMODE_XML);
  }

  @Nonnull
  public IAS4DuplicateManager createDuplicateManager () throws Exception
  {
    return new AS4DuplicateManager (INCOMING_DUPLICATE_XML);
  }
}