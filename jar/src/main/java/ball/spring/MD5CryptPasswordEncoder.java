package ball.spring;
/*-
 * ##########################################################################
 * Reusable Spring Components
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2018 - 2021 Allen D. Ball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ##########################################################################
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Dovecot compatible {@link PasswordEncoder} implementation.  MD5-CRYPT
 * reference implementation available at
 * {@link.uri https://github.com/dovecot/core/blob/master/src/auth/password-scheme-md5crypt.c target=newtab password-scheme-md5crypt.c}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Service
@ToString @Log4j2
public class MD5CryptPasswordEncoder extends DelegatingPasswordEncoder {
    private static final String MD5_CRYPT = "MD5-CRYPT";
    private static final HashMap<String,PasswordEncoder> MAP = new HashMap<>();

    static {
        MAP.put(MD5_CRYPT, MD5Crypt.INSTANCE);
        MAP.put("CLEAR", NoCrypt.INSTANCE);
        MAP.put("CLEARTEXT", NoCrypt.INSTANCE);
        MAP.put("PLAIN", NoCrypt.INSTANCE);
        MAP.put("PLAINTEXT", NoCrypt.INSTANCE);
    }

    private static final Random RANDOM = new Random();

    /**
     * Sole constructor.
     */
    public MD5CryptPasswordEncoder() {
        super(MD5_CRYPT, MAP);

        setDefaultPasswordEncoderForMatches(PasswordEncoderFactories.createDelegatingPasswordEncoder());
    }

    @NoArgsConstructor @ToString
    private static class NoCrypt implements PasswordEncoder {
        private static final String SALT =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        private static final String ITOA64 =
            "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        public static final NoCrypt INSTANCE = new NoCrypt();

        @Override
        public String encode(CharSequence raw) {
            return raw.toString();
        }

        @Override
        public boolean matches(CharSequence raw, String encoded) {
            return raw.toString().equals(encoded);
        }

        protected String salt(int length) {
            StringBuilder buffer = new StringBuilder();

            while (buffer.length() < length) {
                int index = (int) (RANDOM.nextFloat() * SALT.length());

                buffer.append(SALT.charAt(index));
            }

            return buffer.toString();
        }

        protected String itoa64(long value, int size) {
            StringBuilder buffer = new StringBuilder();

            while (--size >= 0) {
                buffer.append(ITOA64.charAt((int) (value & 0x3f)));

                value >>>= 6;
            }

            return buffer.toString();
        }
    }

    @NoArgsConstructor @ToString
    private static class MD5Crypt extends NoCrypt {
        private static final String MD5 = "md5";
        private static final String MAGIC = "$1$";
        private static final int SALT_LENGTH = 8;

        public static final MD5Crypt INSTANCE = new MD5Crypt();

        @Override
        public String encode(CharSequence raw) {
            return encode(raw.toString(), salt(SALT_LENGTH));
        }

        private String encode(String raw, String salt) {
            if (salt.length() > SALT_LENGTH) {
                salt = salt.substring(0, SALT_LENGTH);
            }

            return (MAGIC + salt + "$"
                    + encode(raw.getBytes(UTF_8), salt.getBytes(UTF_8)));
        }

        private String encode(byte[] password, byte[] salt) {
            byte[] bytes = null;

            try {
                MessageDigest ctx = MessageDigest.getInstance(MD5);
                MessageDigest ctx1 = MessageDigest.getInstance(MD5);

                ctx.update(password);
                ctx.update(MAGIC.getBytes(UTF_8));
                ctx.update(salt);

                ctx1.update(password);
                ctx1.update(salt);
                ctx1.update(password);
                bytes = ctx1.digest();

                for (int i = password.length; i > 0;  i -= 16) {
                    ctx.update(bytes, 0, (i > 16) ? 16 : i);
                }

                for (int i = 0; i < bytes.length; i += 1) {
                    bytes[i] = 0;
                }

                for (int i = password.length; i != 0; i >>>= 1) {
                    if ((i & 1) != 0) {
                        ctx.update(bytes, 0, 1);
                    } else {
                        ctx.update(password, 0, 1);
                    }
                }

                bytes = ctx.digest();

                for (int i = 0; i < 1000; i += 1) {
                    ctx1 = MessageDigest.getInstance(MD5);

                    if ((i & 1) != 0) {
                        ctx1.update(password);
                    } else {
                        ctx1.update(bytes, 0, 16);
                    }

                    if ((i % 3) != 0) {
                        ctx1.update(salt);
                    }

                    if ((i % 7) != 0) {
                        ctx1.update(password);
                    }

                    if ((i & 1) != 0) {
                        ctx1.update(bytes, 0, 16);
                    } else {
                        ctx1.update(password);
                    }

                    bytes = ctx1.digest();
                }
            } catch (NoSuchAlgorithmException exception) {
                throw new IllegalStateException(exception);
            }

            StringBuilder result =
                new StringBuilder()
                .append(combine(bytes[0], bytes[6], bytes[12], 4))
                .append(combine(bytes[1], bytes[7], bytes[13], 4))
                .append(combine(bytes[2], bytes[8], bytes[14], 4))
                .append(combine(bytes[3], bytes[9], bytes[15], 4))
                .append(combine(bytes[4], bytes[10], bytes[5], 4))
                .append(combine((byte) 0, (byte) 0, bytes[11], 2));

            return result.toString();
        }

        private String combine(byte b0, byte b1, byte b2, int size) {
            return itoa64(((((long) b0) & 0xff) << 16)
                          | ((((long) b1) & 0xff) << 8)
                          | (((long) b2) & 0xff),
                          size);
        }

        @Override
        public boolean matches(CharSequence raw, String encoded) {
            String salt = null;

            if (encoded.startsWith(MAGIC)) {
                salt = encoded.substring(MAGIC.length()).split("[$]")[0];
            } else {
                throw new IllegalArgumentException("Invalid format");
            }

            return encoded.equals(encode(raw.toString(), salt));
        }
    }
}
