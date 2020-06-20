/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.covidwatch.android.exposurenotification;

import android.security.keystore.KeyProperties;

import com.google.common.io.BaseEncoding;

import org.covidwatch.android.exposurenotification.proto.SignatureInfo;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;

import timber.log.Timber;

/**
 * Signs diagnosis key files.
 *
 * <p>Uses a randomly generated public/private keypair to sign files.
 */
public class KeyFileSigner {

  private static final String TAG = "KeyFileSigner";

  private static final String EC_PARAM_SPEC_NAME = "secp256r1";
  private static final String SIG_ALGO = "SHA256withECDSA";
  // http://oid-info.com/get/1.2.840.10045.4.3.2
  private static final String SIG_ALGO_OID = "1.2.840.10045.4.3.2";
  static final String SIGNATURE_ID = "test-signature-id";
  static final String SIGNATURE_VERSION = "test-signature-version";
  private static final BaseEncoding BASE64 = BaseEncoding.base64();

  private KeyPair keyPair;

  /** Private constructor with static creator method, for singleton operation. */
  public KeyFileSigner() {
    init();
  }


  private void init() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC);
      keyGen.initialize(new ECGenParameterSpec(EC_PARAM_SPEC_NAME));
      // Creates a random key each time.
      keyPair = keyGen.generateKeyPair();
    } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
      // TODO: Better exception.
      Timber.e(e);
      throw new RuntimeException(e);
    }
  }

  byte[] sign(byte[] message) {
    checkKeyStoreInit();
    try {
      Signature sig = Signature.getInstance(SIG_ALGO);
      sig.initSign(keyPair.getPrivate());
      sig.update(message);
      return sig.sign();
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      // TODO: Better exception.
      Timber.e(e);
      throw new RuntimeException(e);
    }
  }

  SignatureInfo signatureInfo() {
    // KeyStore init is not strictly required here, but this sig info is useless without KeyStore.
    checkKeyStoreInit();
    return SignatureInfo.newBuilder()
        .setVerificationKeyId(SIGNATURE_ID)
        .setVerificationKeyVersion(SIGNATURE_VERSION)
        .setSignatureAlgorithm(SIG_ALGO_OID)
        .build();
  }

  KeyPair getKeyPair() {
    return keyPair;
  }

  String getPublicKeyBase64() {
    return BASE64.encode(keyPair.getPublic().getEncoded());
  }

  private void checkKeyStoreInit() {
    if (keyPair == null) {
      throw new IllegalStateException(
          "KeyPair was not initialised. That really shouldn't be possible.");
    }
  }
}
