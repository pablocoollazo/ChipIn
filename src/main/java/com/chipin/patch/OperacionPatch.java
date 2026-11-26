package com.chipin.patch;

import tools.jackson.databind.JsonNode;

public record OperacionPatch(TipoOperacionPatch op, String path, JsonNode value) {}
