/*  NightlyTestsPluginTest.kt
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

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.testfixtures.ProjectBuilder

import com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties


/**
 *  NightlyTestsPluginTest:
 *  =======================
 *
 *  JUnit test cases on the NightlyTestsPlugin
 */
open class NightlyTestsPluginTest {
    companion object {
        // Nice cheating: this::class.java.protectionDomain.codeSource.location -> build\classes\kotlin\test
        private val testClassLocation = File(this::class.java.protectionDomain.codeSource.location.toURI())

        // path to example properties files in "resources" folder
        private val projectPropertiesPath   = resource("project.properties")
        private val wrongPropertiesPath     = resource("project_wrong.properties")

        // properties containing file content
        private val projectProperties   = Properties()
        private val wrongProperties     = Properties()


        /** internally used simplified resource loader */
        private fun resource(path: String): String {
            return this::class.java.classLoader.getResource(path)!!.path.replace("%20", " ")
        }


        /** 0) Configuration to read properties once before running multiple tests using them */
        @Throws(IOException::class)
        @BeforeClass @JvmStatic fun configureTestsuite() {
            // read "project" properties into local properties object
            projectProperties.load(FileInputStream(projectPropertiesPath))
            wrongProperties.load(FileInputStream(wrongPropertiesPath))
        }
    }


    /** 1) Tests only applying the plugin (without project properties used for configuration) */
    @Test fun testApplyPluginWithoutPropertiesToProject() {
        val project = ProjectBuilder.builder().build()

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(NightlyTestsPlugin::class.java)
        } catch (e : Exception) {
            // assert applying did not work because of missing property
            Assert.assertEquals(MissingPropertiesEntryException::class, e.cause!!::class)
        }

        // assert that plugin is not loaded
        Assert.assertFalse(project.plugins.hasPlugin(NightlyTestsPlugin::class.java))
    }


    /** 2) Tests only applying the plugin using system property (without project properties) */
    @Test fun testApplyPluginWithNightlyVariableToProject() {
        val project = ProjectBuilder.builder().build()

        restoreSystemProperties {
            System.setProperty("BUILDSERVER", "NIGHTLYBUILD")
            Assert.assertEquals("NIGHTLYBUILD", System.getProperty("BUILDSERVER"))

            // apply plugin
            project.pluginManager.apply(NightlyTestsPlugin::class.java)

            // assert that plugin is loaded
            Assert.assertTrue(project.plugins.hasPlugin(NightlyTestsPlugin::class.java))
        }
    }


    /** 3) Tests only applying the plugin (with project properties used for configuration) */
    @Test fun testApplyPluginWithPropertiesToProject() {
        val project = ProjectBuilder.builder().build()

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // read "project" properties file and secretly append to project
        projectProperties.forEach {
            val key : String = it.key as String
            val value : Any? = it.value

            propertiesExtension.set(key, value)
        }

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(NightlyTestsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work due to no task of type Test found
            Assert.assertEquals(TaskWithTypeTestNotFoundException::class, e.cause!!::class)
        }

        // assert that plugin is not loaded
        Assert.assertFalse(project.plugins.hasPlugin(NightlyTestsPlugin::class.java))
    }


    /** 4) Tests only applying the plugin (with wrong project properties used for configuration) */
    @Test fun testApplyPluginWithWrongPropertiesToProject() {
        val project = ProjectBuilder.builder().build()

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // read "project" properties file and secretly append to project
        wrongProperties.forEach {
            val key : String = it.key as String
            val value : Any? = it.value

            propertiesExtension.set(key, value)
        }

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(NightlyTestsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work due to now tests provided to run sequential
            Assert.assertEquals(PropertiesEntryInvalidException::class, e.cause!!::class)
        }

        // assert that plugin is not loaded
        Assert.assertFalse(project.plugins.hasPlugin(NightlyTestsPlugin::class.java))
    }


    /** 5) Tests only applying the plugin (with project properties used for configuration and a "test" task) */
    @Test fun testApplyPluginWithPropertiesAndTestTaskToProject() {
        val project = ProjectBuilder.builder().build()

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // read "project" properties file and secretly append to project
        projectProperties.forEach {
            val key : String = it.key as String
            val value : Any? = it.value

            propertiesExtension.set(key, value)
        }

        // add task named "test" to make plugin work
        project.tasks.register("test", org.gradle.api.tasks.testing.Test::class.java)

        // apply plugin
        project.pluginManager.apply(NightlyTestsPlugin::class.java)

        // assert that plugin is loaded
        Assert.assertTrue(project.plugins.hasPlugin(NightlyTestsPlugin::class.java))
    }


    /** 6) Tests only applying the plugin (root project) */
    @Test fun testApplyPluginWithPropertiesRootProject() {
        val project = ProjectBuilder.builder().withProjectDir(testClassLocation.parentFile).build()
        val subProject = ProjectBuilder.builder().withParent(project).build()

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // read "project" properties file and secretly append to project
        projectProperties.forEach {
            val key : String = it.key as String
            val value : Any? = it.value

            propertiesExtension.set(key, value)
        }

        // add task named "test" to make plugin work
        subProject.tasks.register("test", org.gradle.api.tasks.testing.Test::class.java)

        // apply plugin
        subProject.pluginManager.apply(NightlyTestsPlugin::class.java)

        // assert that plugin is loaded
        Assert.assertTrue(subProject.plugins.hasPlugin(NightlyTestsPlugin::class.java))
    }


    /** 7) Tests applying the plugin and evaluates the correct configuration of the "test" task */
    @Test fun testEvaluateCorrectnessTestTask() {
        val project = ProjectBuilder.builder().build()

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // read "project" properties file and secretly append to project
        projectProperties.forEach {
            val key : String = it.key as String
            val value : Any? = it.value

            propertiesExtension.set(key, value)
        }

        // add task named "test" to make plugin work
        project.tasks.register("test", org.gradle.api.tasks.testing.Test::class.java)

        // apply plugin
        project.pluginManager.apply(NightlyTestsPlugin::class.java)

        // assert that "test" task is configured correctly
        val findByName = project.tasks.findByName("test")!! as org.gradle.api.tasks.testing.Test
        val filter = findByName.filter.excludePatterns

        NightlyTestsPlugin.parseExcludeList(
            projectProperties["plugins.nightlytests.listOfTests"]!! as String
        ).forEach {
            Assert.assertTrue(filter.contains(it))
        }
    }
}
