#version 460 core

layout(location = 0) in vec2 position;
layout(location = 1) in vec3 inColor;
layout(location = 2) in vec2 inTexCoord;

out vec3 color;
out vec2 texCoord;

layout(location = 0) uniform mat4 transform;

void main() {
	gl_Position = transform * vec4(position, 0, 1);
	color = inColor;
	texCoord = inTexCoord;
}
