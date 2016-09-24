/*
 *   Copyright (C) 2016 Pawel Badenski
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.halik.intellij.server.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

public class FlowEntry_V2 implements FlowEntry {
    private String line;
    private List<String> classIndex;

    public FlowEntry_V2(String line, List<String> classIndex) {
        this.line = line;
        this.classIndex = classIndex;
    }

    @Override
    public Optional<String> className() {
        return extractClassName(line);
    }

    private Optional<String> extractClassName(String line) {
        JsonParser jsonParser = new JsonParser();
        try {
            return Optional
                    .ofNullable(line)
                    .map(l -> {
                        JsonReader jsonReader = new JsonReader(new StringReader(l));
                        jsonReader.setLenient(true);
                        return jsonParser.parse(jsonReader);
                    })
                    .filter(JsonElement::isJsonObject)
                    .map(jsonElement -> jsonElement.getAsJsonObject().getAsJsonPrimitive("c").getAsInt())
                    .map(aClassIndexValue -> classIndex.get(aClassIndexValue))
                    .map(className -> className.replaceAll("/", "."));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
