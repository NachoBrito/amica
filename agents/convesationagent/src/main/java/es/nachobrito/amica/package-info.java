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
package es.nachobrito.amica;

import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import com.cosium.json_schema_to_java_record_api.JsonSchemaConfiguration;
import com.cosium.json_schema_to_java_record_api.JsonSchemaFileLocation;
