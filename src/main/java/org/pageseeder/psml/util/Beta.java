/*
 * Copyright (c) 1999-2025 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.util;

import java.lang.annotation.*;

/**
 * Indicates that a public API (class, method, field, etc.) is subject to
 * incompatible changes, or even removal, in a future release.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, 
         ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Beta {
}
