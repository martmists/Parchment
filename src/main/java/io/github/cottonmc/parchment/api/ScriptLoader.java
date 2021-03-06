package io.github.cottonmc.parchment.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nullable;
import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;

import io.github.cottonmc.parchment.impl.ScriptLoaderImpl;
import org.apache.commons.io.IOUtils;

import net.minecraft.util.Identifier;

/**
 * Class for loading runnable scripts.
 */
public interface ScriptLoader {
	ScriptLoader INSTANCE = new ScriptLoaderImpl();

	/**
	 * Load a script from a string.
	 * @param factory The factory to assemble the loaded script with.
	 * @param id The ID of the script, including extension.
	 * @param contents The string contents of the script.
	 * @return The prepared script object.
	 * @throws IllegalArgumentException if there is no script engine for the extension.
	 */
	@Nullable
	Script loadScript(ScriptFactory factory, Identifier id, String contents) throws IllegalArgumentException;

	/**
	 * Load a script from an input stream.
	 * @param factory The factory to assemble the loaded script with.
	 * @param id The ID of the script, including extension.
	 * @param contents The contents of the script as an input stream.
	 * @return The prepared script object.
	 * @throws IOException if the input stream cannot be converted to a string.
	 * @throws IllegalArgumentException if there is no script engine for the extension.
	 */
	@Nullable
	default Script loadScript(ScriptFactory factory, Identifier id, InputStream contents) throws IOException, IllegalArgumentException {
		return loadScript(factory, id, IOUtils.toString(contents, Charset.defaultCharset()));
	}

	/**
	 * Interface used to build prepared scripts.
	 */
	interface ScriptFactory {
		/**
		 * A script factory which creates simple script wrappers.
		 */
		ScriptFactory SIMPLE = SimpleScript::new;

		/**
		 * A script factory which creates script wrappers that compile beforehand.
		 */
		ScriptFactory SIMPLE_COMPILABLE = (engine, id, contents) -> {
			if (!(engine instanceof Compilable)) return null; //TODO: log?
			return new SimpleCompilableScript(engine, id, contents);
		};

		/**
		 * A script factory which creates script wrappers that can invoke functions.
		 */
		ScriptFactory SIMPLE_INVOCABLE = (engine, id, contents) -> {
			if (!(engine instanceof Invocable)) return null; //TODO: log?
			return new SimpleInvocableScript(engine, id, contents);
		};

		/**
		 * A script factory which creates script wrappers that compile beforehand and can invoke functions.
		 */
		ScriptFactory SIMPLE_FULL = (engine, id, contents) -> {
			if (!(engine instanceof Compilable) || !(engine instanceof Invocable)) return null; //TODO: log?
			return new SimpleFullScript(engine, id, contents);
		};

		/**
		 * @param engine The script engine used for this script.
		 * @param id The ID of this script.
		 * @param contents The string contents of the script.
		 * @return The prepared script wrapper.
		 */
		@Nullable
		Script build(ScriptEngine engine, Identifier id, String contents);
	}
}
