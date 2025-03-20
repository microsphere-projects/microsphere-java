/*
 * Copyright 2002-2019 the original author or authors.
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
 */

package io.microsphere.annotation;


import javax.annotation.meta.TypeQualifierNickname;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A common Microsphere annotation to declare that annotated elements cannot be {@code null}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see javax.annotation.Nonnull
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@javax.annotation.Nonnull
@TypeQualifierNickname
public @interface Nonnull {
}
