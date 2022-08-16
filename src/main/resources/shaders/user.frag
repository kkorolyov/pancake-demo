#version 460 core

in vec3 color;
in vec2 texCoord;

out vec4 result;

layout(binding = 0) uniform sampler2D sampler;

void main() {
	result = vec4(color, 1) * texture(sampler, texCoord);
}
