/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.annotation.processor;

import io.microsphere.lang.function.ThrowableConsumer;
import io.microsphere.lang.function.ThrowableFunction;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.microsphere.annotation.processor.util.FilerUtils.exists;
import static io.microsphere.annotation.processor.util.MessagerUtils.printNote;
import static io.microsphere.annotation.processor.util.MessagerUtils.printWarning;
import static io.microsphere.util.ExceptionUtils.wrap;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * The {@link ProcessingEnvironment} Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ProcessingEnvironment
 * @since 1.0.0
 */
public class ResourceProcessor {

    static final boolean FOR_READING = false;

    static final boolean FOR_WRITING = true;

    private final ProcessingEnvironment processingEnv;

    private final Location location;

    private final CharSequence moduleAndPackage;

    private final FilerProcessor filerProcessor;

    private final Map<String, FileObject> fileObjectsCache;

    public ResourceProcessor(ProcessingEnvironment processingEnv, Location location) {
        this(processingEnv, location, "");
    }

    public ResourceProcessor(ProcessingEnvironment processingEnv, Location location, CharSequence moduleAndPackage) {
        this.processingEnv = processingEnv;
        this.location = location;
        this.filerProcessor = new FilerProcessor(processingEnv);
        this.moduleAndPackage = moduleAndPackage;
        this.fileObjectsCache = new HashMap<>();
        addShutdownHookCallback(fileObjectsCache::clear);
    }

    public <T> T processInResource(String resourceName, boolean forWriting, ThrowableFunction<Optional<FileObject>, T> resourceCallback) {
        return processInResource(resourceName, forWriting, resourceCallback, e -> {
            printWarning(processingEnv, "[ResourceProcessor] Failed to process the resource[name : '{}' , forWriting : {}]", resourceName, forWriting, e);
            throw wrap(e, RuntimeException.class);
        });
    }

    public <T> T processInResource(String resourceName, boolean forWriting, ThrowableFunction<Optional<FileObject>, T> resourceCallback,
                                   Function<Throwable, T> exceptionHandler) {
        return filerProcessor.processInFiler(filer -> {
            String key = (forWriting ? "W" : "R") + "@" + resourceName;
            FileObject resource = fileObjectsCache.get(key);
            if (resource == null) {
                resource = forWriting ? filer.createResource(location, moduleAndPackage, resourceName) :
                        filer.getResource(location, moduleAndPackage, resourceName);
                fileObjectsCache.put(key, resource);
            }

            final Optional<FileObject> resourceRef;
            if (forWriting) {
                resourceRef = of(resource);
            } else {
                if (exists(resource)) {
                    resourceRef = of(resource);
                } else {
                    printNote(processingEnv, "[ResourceProcessor] The resource[relative : '{}' , name : '{}'] does not exist",
                            resourceName, resource.getName());
                    resourceRef = empty();
                }
            }
            return resourceCallback.apply(resourceRef);
        }, (filter, e) -> {
            printWarning(processingEnv, "[ResourceProcessor] Failed to process the resource[name : '{}']", resourceName, e);
            return exceptionHandler.apply(e);
        });
    }

    public Optional<FileObject> getResource(String resourceName, boolean forWriting) {
        return processInResource(resourceName, forWriting, resource -> resource);
    }

    public <T> Optional<T> processInResourceInputStream(String resourceName, ThrowableFunction<InputStream, T> streamCallback) {
        return processInResourceInputStream(resourceName, streamCallback, (resource, e) -> {
            printWarning(processingEnv, "[ResourceProcessor] Failed to process the InputStream of the resource[relative : '{}' , name : '{}']", resourceName, resource.getName(), e);
            throw wrap(e, RuntimeException.class);
        });
    }

    public <T> Optional<T> processInResourceInputStream(String resourceName, ThrowableFunction<InputStream, T> streamCallback,
                                                        BiFunction<FileObject, Throwable, T> exceptionHandler) {
        return processInResource(resourceName, FOR_READING, resourceRef -> resourceRef.map(resource -> {
            try (InputStream inputStream = resource.openInputStream()) {
                return streamCallback.apply(inputStream);
            } catch (Throwable e) {
                return exceptionHandler.apply(resource, e);
            }
        }));
    }

    public <T> Optional<T> processInResourceReader(String resourceName, ThrowableFunction<Reader, T> readerCallback) {
        return processInResourceReader(resourceName, readerCallback, (resource, e) -> {
            printWarning(processingEnv, "[ResourceProcessor] Failed to process the Reader of the resource[relative : '{}' , name : '{}']", resourceName, resource.getName(), e);
            throw wrap(e, RuntimeException.class);
        });
    }

    public <T> Optional<T> processInResourceReader(String resourceName, ThrowableFunction<Reader, T> readerCallback, BiFunction<FileObject, Throwable, T> exceptionHandler) {
        return processInResource(resourceName, FOR_READING, resourceRef -> resourceRef.map(resource -> {
            try (Reader reader = resource.openReader(true)) {
                return readerCallback.apply(reader);
            } catch (Throwable e) {
                return exceptionHandler.apply(resource, e);
            }
        }));
    }

    public <T> Optional<T> processInResourceContent(String resourceName, ThrowableFunction<CharSequence, T> contentCallback) {
        return processInResourceContent(resourceName, contentCallback, (resource, e) -> {
            printWarning(processingEnv, "[ResourceProcessor] Failed to process the content of the resource[relative : '{}' , name : '{}']", resourceName, resource.getName(), e);
            throw wrap(e, RuntimeException.class);
        });
    }

    public <T> Optional<T> processInResourceContent(String resourceName, ThrowableFunction<CharSequence, T> contentCallback, BiFunction<FileObject, Throwable, T> exceptionHandler) {
        return processInResource(resourceName, FOR_READING, resourceRef -> resourceRef.map(resource -> {
            try {
                CharSequence content = resource.getCharContent(true);
                return contentCallback.apply(content);
            } catch (Throwable e) {
                return exceptionHandler.apply(resource, e);
            }
        }));
    }

    public void processInResourceOutputStream(String resourceName, ThrowableConsumer<OutputStream> streamConsumer) {
        processInResourceOutputStream(resourceName, streamConsumer, (resource, e) -> {
            printWarning(processingEnv, "[ResourceProcessor] Failed to process the OutputStream of the resource[relative : '{}' , name : '{}']", resourceName, resource.getName(), e);
            throw wrap(e, RuntimeException.class);
        });
    }

    public void processInResourceOutputStream(String resourceName, ThrowableConsumer<OutputStream> streamConsumer,
                                              BiConsumer<FileObject, Throwable> exceptionHandler) {
        processInResource(resourceName, FOR_WRITING, resourceRef -> resourceRef.map(resource -> {
            try (OutputStream outputStream = resource.openOutputStream()) {
                streamConsumer.accept(outputStream);
                outputStream.flush();
            } catch (Throwable e) {
                exceptionHandler.accept(resource, e);
            }
            return null;
        }));
    }

    public void processInResourceWriter(String resourceName, ThrowableConsumer<Writer> writerConsumer) {
        processInResourceWriter(resourceName, writerConsumer, (resource, e) -> {
            printWarning(processingEnv, "[ResourceProcessor] Failed to process the Writer of the resource[relative : '{}' , name : '{}']", resourceName, resource.getName(), e);
            throw wrap(e, RuntimeException.class);
        });
    }

    public void processInResourceWriter(String resourceName, ThrowableConsumer<Writer> writerConsumer, BiConsumer<FileObject, Throwable> exceptionHandler) {
        processInResource(resourceName, FOR_WRITING, resourceRef -> resourceRef.map(resource -> {
            try (Writer writer = resource.openWriter()) {
                writerConsumer.accept(writer);
                writer.flush();
            } catch (Throwable e) {
                exceptionHandler.accept(resource, e);
            }
            return null;
        }));
    }
}
