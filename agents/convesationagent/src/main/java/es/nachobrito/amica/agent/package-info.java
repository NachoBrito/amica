/*
 *    Copyright 2025 Nacho Brito
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@GenerateRecordsFromJsonSchemas(
    schemaRootFileLocations = {
      @JsonSchemaFileLocation(
          moduleAndPackage = "es.nachobrito.amica.domain.model.message.payload",
          relativeName = "UserRequest.json")
    },
    schemaConfigurations = {
      @JsonSchemaConfiguration(
          schemaId = "UserRequest",
          javaTypeQualifiedName = "es.nachobrito.amica.domain.model.message.payload.UserRequest",
          javaInterfaceQualifiedNames = "es.nachobrito.amica.domain.model.message.MessagePayload")
    })
package es.nachobrito.amica.agent;

import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import com.cosium.json_schema_to_java_record_api.JsonSchemaConfiguration;
import com.cosium.json_schema_to_java_record_api.JsonSchemaFileLocation;
