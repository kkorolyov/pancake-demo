module dev.kkorolyov.pancake.demo {
	requires transitive java.desktop;
	requires transitive kotlin.stdlib;
	requires transitive kotlin.stdlib.jdk7;
	requires transitive kotlin.stdlib.jdk8;

	requires transitive dev.kkorolyov.flub;

	requires transitive org.slf4j;
	requires org.apache.logging.log4j;

	requires org.lwjgl;
	requires org.lwjgl.natives;
	requires org.lwjgl.glfw;

	requires transitive dev.kkorolyov.pancake.platform;
	requires transitive dev.kkorolyov.pancake.core;
	requires transitive dev.kkorolyov.pancake.graphics;
	requires transitive dev.kkorolyov.pancake.graphics.gl;
	requires transitive dev.kkorolyov.pancake.audio.al;
	requires transitive dev.kkorolyov.pancake.input;
	requires transitive dev.kkorolyov.pancake.input.glfw;

	requires dev.kkorolyov.pancake.editor;
	requires dev.kkorolyov.pancake.editor.core;

	exports dev.kkorolyov.pancake.demo;

	opens shaders;
}
