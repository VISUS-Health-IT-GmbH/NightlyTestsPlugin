/*  NightlyTestsException.kt
 *
 *  Copyright (C) 2021, VISUS Health IT GmbH
 *  This software and supporting documentation were developed by
 *    VISUS Health IT GmbH
 *    Gesundheitscampus-Sued 15-17
 *    D-44801 Bochum, Germany
 *    http://www.visus.com
 *    mailto:info@visus.com
 *
 *  -> see LICENCE at root of repository
 */
package com.visus.infrastructure

import org.gradle.api.InvalidUserDataException


/**
 *  Base extension for every extension thrown by this plugin
 *
 *  @author thahnen
 */
open class NightlyTestsException(message: String) : InvalidUserDataException(message)


/**
 *  Exception thrown when no configuration provided in projects gradle.properties file
 */
open class MissingPropertiesEntryException(message: String) : NightlyTestsException(message)


/**
 *  Exception thrown when value of necessary property entry is invalid (no content)
 */
open class PropertiesEntryInvalidException(message: String) : NightlyTestsException(message)


/**
 *  Thrown when this plugin is applied to a project which does not have any Gradle tasks of type "Test" (necessary for
 *  disabling tests only running nightly)!
 */
open class TaskWithTypeTestNotFoundException(message: String) : NightlyTestsException(message)
