package net.gradleutil.conf.annotation

import groovy.transform.AnnotationCollector
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
@AnnotationCollector
@interface ToStringIncludeNames { }

