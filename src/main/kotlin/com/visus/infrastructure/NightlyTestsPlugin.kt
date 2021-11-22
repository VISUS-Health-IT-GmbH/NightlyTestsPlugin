/*  NightlyTestsPlugin.kt
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

import java.util.Properties

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.InvalidUserDataException


/**
 *  NightlyTestsPlugin:
 *  ===================
 *
 *  @author thahnen
 *
 *  Plugin to configure special tests to only run in nightly build
 */
open class NightlyTestsPlugin : Plugin<Project> {

    /** Companion object to use these internal function inside jUnit tests again */
    companion object {
        // identifiers of the properties / environment variables needed by this plugin
        private const val KEY_LISTOFTESTS = "plugins.nightlytests.listOfTests"
        private const val ENV_BUILDSERVER = "BUILDSERVER"

        /**
         *  Parses a property to return a list of excluded projects
         *
         *  @param property the property holding the "list"
         *  @return actual list of (unique) projects
         */
        internal fun parseExcludeList(property: String) : List<String> = property.split(",").distinct()
    }


    /** Overrides the abstract "apply" function */
    override fun apply(target: Project) {
        // 1) check if environment variable set to anything with "nightly"
        if (System.getProperties().containsKey(ENV_BUILDSERVER)
            && (System.getProperties()[ENV_BUILDSERVER] as String).contains("nightly", ignoreCase = true)) {
            return
        }

        // 2) read values from project's "gradle.properties" file
        val properties = readProjectProperties(target)

        // 3) create list of tests from property
        val listOfTests = parseExcludeList(properties["listOfTests"] as String)
        if (listOfTests.isEmpty() || (listOfTests.size == 1 && listOfTests[0] == "")) {
            throw NightlyTestsException("Plugin property 'listOfTests' empty or not correctly set!")
        }

        // 4) exclude all given tests from any Gradle "test" task
        val testTasks = target.tasks.withType(Test::class.java)
        if (testTasks.size == 0) {
            throw NightlyTestsException("No test tasks found, therefore applying this plugin is not necessary!")
        }

        testTasks.forEach {
            it.filter {
                listOfTests.forEach { test ->
                    @Suppress("UnstableApiUsage")
                    this.excludeTestsMatching(test)
                }
            }
        }
    }


    /**
     *  Reads the project properties which are used for configuration the new single threaded test task!
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @return the specific properties key-value pairs read from the project properties itself
     *  @throws NightlyTestsException when project properties are configured wrong
     */
    @Throws(NightlyTestsException::class)
    private fun readProjectProperties(target: Project) : Properties {
        val properties = Properties()

        if (target.properties.containsKey(KEY_LISTOFTESTS)) {
            properties["listOfTests"] = target.properties[KEY_LISTOFTESTS]
        } else if (target.rootProject.properties.containsKey(KEY_LISTOFTESTS)) {
            properties["listOfTests"] = target.rootProject.properties[KEY_LISTOFTESTS]
        }

        if (properties.size == 0) {
            // This is not possible under normal circumstances!
            throw NightlyTestsException("Plugin specific property missing in project properties or left blank!")
        }

        return properties
    }
}


/**
 *  NightlyTestsException:
 *  =====================
 *
 *  Custom exception to make it more recognizable if something fails (I hope not)
 */
internal class NightlyTestsException(message: String) : InvalidUserDataException(message)
