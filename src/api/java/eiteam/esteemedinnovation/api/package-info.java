/**
 * This is the base API package for the Esteemed Innovation mod.
 *
 * This documentation is <i>not</i> for the documentation of the mod of the same name, Esteemed Innovation. This
 * documentation is for the Esteemed Innovation API, which mod developers can use to interact with the mod, or build
 * their own systems similar to those within the mod.
 *
 * <h2>Prerequisites</h2>
 * It is crucial that you have at least a basic understanding of Java and the NeoForge API.
 *
 * <h2>Getting started</h2>
 * In order to add the EI API to your project, add the following to your Gradle buildscript. This does not go in the
 * {@code buildscript} block.
 * <pre>
 * <code>
 *     repositories {
 *         maven {
 *             url "http://dl.bintray.com/esteemed-innovation/Esteemed-Innovation"
 *         }
 *     }
 *
 *     dependencies {
 *         compile "eiteam.esteemedinnovation:Esteemed-Innovation:VERSION:Esteemed-Innovation-VERSION-api"
 *     }
 * </code>
 * </pre>
 * where {@code VERSION} is the version of Esteemed Innovation.
 * <p>
 * The actual mod source is not hosted in the maven repository. If you need the whole mod, simply download it from
 * CurseForge and add it to your repository as a library.
 * @see <a href="https://ftb.fandom.com/Esteemed_Innovation_(Mod)">the FTB wiki</a> for the documentation of the mod.
 */
package eiteam.esteemedinnovation.api;