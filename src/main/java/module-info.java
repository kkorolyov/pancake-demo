module dev.kkorolyov.pancake.demo {
	requires transitive kotlin.stdlib;

	requires transitive javafx.base;
	requires transitive javafx.graphics;
	requires transitive tornadofx;

	requires transitive org.slf4j;
	requires transitive org.apache.logging.log4j;

	requires transitive dev.kkorolyov.pancake.platform;
	requires transitive dev.kkorolyov.pancake.core;
	requires transitive dev.kkorolyov.pancake.graphics.jfx;
	requires transitive dev.kkorolyov.pancake.audio.jfx;
	requires transitive dev.kkorolyov.pancake.input.jfx;

	requires dev.kkorolyov.pancake.editor;
	requires dev.kkorolyov.pancake.editor.core;

	exports dev.kkorolyov.pancake.demo;
}
