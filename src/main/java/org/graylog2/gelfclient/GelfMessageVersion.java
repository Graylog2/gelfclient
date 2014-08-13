/*
 * Copyright 2014 TORCH GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graylog2.gelfclient;

/**
 * The version of the <a href="http://graylog2.org/gelf#specs">GELF specification</a>
 * used for a {@link GelfMessage}.
 */
public enum GelfMessageVersion {
    V1_1("1.1");

    private final String versionString;

    GelfMessageVersion(String versionString) {
        this.versionString = versionString;
    }

    public String toString() {
        return versionString;
    }
}
