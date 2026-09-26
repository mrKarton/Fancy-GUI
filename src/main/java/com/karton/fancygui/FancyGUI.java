package com.karton.fancygui;

import com.karton.fancygui.gui.server.buttons.ButtonsRegistrator;
import com.karton.fancygui.network.FancyGUINetworking;
import com.karton.fancygui.tests.OpenTestScreenCommand;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FancyGUI
		implements ModInitializer {

	public static final String MOD_ID =
			"fancy-gui";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(
					MOD_ID
			);

	public static final String VERSION =
			FabricLoader
					.getInstance()
					.getModContainer(MOD_ID)
					.orElseThrow()
					.getMetadata()
					.getVersion()
					.getFriendlyString();

	public static boolean isDevMode =
			false;

	@Override
	public void onInitialize() {

		LOGGER.info(
				"Fancy GUI version {} loading",
				VERSION
		);

		if (
				VERSION.contains("-dev.")
						|| FabricLoader
						.getInstance()
						.isDevelopmentEnvironment()
		) {
			LOGGER.warn(
					"====================================================="
			);
			LOGGER.warn(
					"Fancy GUI is on development mode as it is -dev build"
			);
			LOGGER.warn(
					"All debug commands enabled"
			);
			LOGGER.warn(
					"Good luck and have fun :D"
			);
			LOGGER.warn(
					"====================================================="
			);

			isDevMode = true;

			OpenTestScreenCommand.registerCommand();
		}

		/*
		 * Items / Polymer.
		 */
		ButtonsRegistrator.register();

		/*
		 * ВАЖНО:
		 *
		 * Больше никаких ScreenTypes.register().
		 *
		 * Мы НЕ регистрируем custom MenuType.
		 */
		FancyGUINetworking.register();

		PolymerResourcePackUtils.addModAssets(
				MOD_ID
		);
	}

	public static Identifier id(
			String path
	) {
		return Identifier.fromNamespaceAndPath(
				MOD_ID,
				path
		);
	}
}