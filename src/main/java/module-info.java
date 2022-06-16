module dev.kkorolyov.pancake.demo {
	requires transitive kotlin.stdlib;
	requires transitive kotlin.stdlib.jdk7;

	requires transitive dev.kkorolyov.flub;

	requires transitive org.slf4j;
	requires org.apache.logging.log4j;

	requires javafx.graphics;
	requires tornadofx;

	requires org.lwjgl.glfw;
	requires org.lwjgl.natives;
	requires org.lwjgl.glfw.natives;
	requires org.lwjgl.opengl.natives;
	requires org.lwjgl.openal.natives;

	requires transitive dev.kkorolyov.pancake.platform;
	requires transitive dev.kkorolyov.pancake.core;
	requires transitive dev.kkorolyov.pancake.graphics.common;
	requires transitive dev.kkorolyov.pancake.graphics.gl;
	requires transitive dev.kkorolyov.pancake.audio.al;
	requires transitive dev.kkorolyov.pancake.input.common;
	requires transitive dev.kkorolyov.pancake.input.glfw;

	requires dev.kkorolyov.pancake.editor;
	requires dev.kkorolyov.pancake.editor.core;

	exports dev.kkorolyov.pancake.demo;

	opens shaders;
}
