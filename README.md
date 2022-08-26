# Purpose

The SWETransform library is intended to be used in multiple services - anywhere that data needs to be
transformed between XML versions of the SWE standards and JSON versions. Additional domain-specific 
transformations may be added in the future as needed.

The XML to JSON transformations follow the OGC best practices as documented in the best practices white
paper from the OGC:
* [JSON Encoding Rules SWE Common/SensorML](http://docs.opengeospatial.org/is/17-011r2/17-011r2.html)

# Project Structure

This library is built as a standard Java jar file with slf4j logging API included but no logging implementations.
All the dependencies are included in the uberjar that is created as part of a `mvn clean install`.

This version of the library utilizes JAXB to unmarshal XML input into Java objects. These objects are then
traversed to generate the corresponding JSON. In the future, this might be changed to utilize a SAX/STAX parser
and transform the XML on the fly during parsing.

## Configuration

There is no real configuration for this library. It is expected that you will include this as a dependency
in your project and just call the appropriate methods. In order to get logging working properly in your
project, you just need to have a configuration file for a logging backend that slf4j will find and utilize.

## Dependencies

The uberjar created by this project includes all the dependencies needed to work properly. This includes:

* jaxb-api
* jackson-databind
* jackson-dataformat-xml
* json-simple
* ogc schema files
  * om-v_2_0
  * sensorML-v_1_0_1
  * sos-v_2_0
  * sweCommon-v_2_0
  * samplingSpatial-v_2_0
* slf4j-api
* jaxb-runtime

# Build

To build the library from the parent directory:
```shell
mvn clean install
```

This will create 2 jar files in the target directory. The jar file names are:

* SWEXmlParser-1.0-SNAPSHOT-shaded.jar
* SWEXmlParser-1.0-SNAPSHOT.jar

The shaded jar contains all the required dependencies for the transformers to work. The non-shaded
jar only containes the actual class files from this project (no dependencies).

# Usage
The library consists of an interface (`SWETransform`) containing static methods to invoke in order to 
transform data from XML format to JSON.
Currently, only the `InsertSensorTransformer` is implemented. Others will be added in time.

`InsertSensorTransformer` has three signatures that can be used - taking as input:
* String
* File
* InputStream

The signatures of those methods are as follows:

```java
public static String insertSensorToJSON(String xml) throws IllegalArgumentException 
public static String insertSensorToJSON(File xmlFile) throws IllegalArgumentException
public static String insertSensorToJSON(InputStream is) throws IllegalArgumentException
```
## Examples
The lone unit test that just runs a sample InsertObservation request through the transformer provides
an indication of how to use the library. You can basically call one of the above functions with your
XML InsertSensor request, and it will return the JSON equivalent.

## Command Line Usage
A `main` method is included in the library so that you can use the library from the command line. With
this you can convert files on the fly that exist on your filesystem.
An example of this using the `trailcam.xml` file included in the `test/resources` directory would look
like the following:
```shell
java -jar target/SWEXmlParser-1.0-SNAPSHOT-shaded.jar  src/test/resources/trailcam.xml
```
Because there is no logging backend configured in the library, some debug messages are output to
the console when you run this way - the output looks like:
```shell
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
{"observableProperty":"urn:gsw:def:phenomenon:radiance\n\t","metadata":{"observationType":"http:\/\/www.opengis.net\/def\/observationType\/OGC-OM\/2.0\/OM_ComplexObservation","featureOfInterestType":"http:\/\/www.opengis.net\/def\/samplingFeatureType\/OGC-OM\/2.0\/SF_SamplingPoint","type":"SosInsertionMetadata"},"procedureDescription":{"characteristics":{"field":[{"uom":{"href":"urn:ogc:def:uom:OGC:1.0:ampHours"},"name":"Battery Amp-hours remaining","type":"Quantity","value":3.6},{"name":"Sensor State","type":"Text","value":"A"},{"name":"Sensor String","type":"Text","value":"Trailcam"},{"name":"Inbound Channel","type":"Text","value":"0001"},{"name":"Device ID","type":"Text","value":"000001"},{"name":"Plan Name","type":"Text","value":"Capture Bigfoot"},{"name":"Plan Owner","type":"Text","value":"Willey Ohioman"},{"name":"Comments","type":"Text","value":"Willey Ohioman"},{"name":"Symbol String","type":"Text","value":"SFGPES------US-"}],"type":"DataRecord"},"identification":{"identifier":[{"name":"uniqueID","definition":"urn:ogc:def:identifierType:OGC:uniqueID","type":"Term","value":"urn:ogc:object:Sensor:Trailcam"},{"name":"uniqueID","definition":"urn:ogc:def:identifier:OGC:1.0:uniqueID","type":"Term","value":"urn:ogc:object:Sensor:Trailcam"},{"name":"longName","definition":"urn:ogc:def:identifier:OGC:1.0:longName","type":"Term","value":"Super Secret Trailcam"},{"name":"shortName","definition":"urn:ogc:def:identifier:OGC:1.0:shortName","type":"Term","value":"Trailcam"}],"type":"IdentifierList"},"keywords":{"type":"KeywordList","keyword":["Trailcam","unattended","ground","sensors","ugs"]},"member":{"outputs":{"output":[{"name":"Message Type","type":"Text","value":""},{"name":"Channel","type":"Text","value":""},{"name":"Detail","type":"Text","value":""},{"name":"Image","type":"Text","value":""}],"type":"OutputList"},"name":"Trailcam","description":"An unattended ground\n\t\t\t\t\t\tsensor with sophisticaed image recognition capabilities that only\n\t\t\t\t\t\ttakes pictures of bigfoot","location":{"srsName":"EPSG:4979","srsDimension":"3","pos":"39.69427 -84.10164 0.0","id":"sensorLocation","type":"Point"},"type":"Component","sweName":"name"},"validTime":{"timePosition":{"value":"2020-01-01T00:00:01Z","indeterminatePosition":"after"},"type":"TimeInstant"},"type":"SensorML"},"procedureDescriptionFormat":"http:\/\/www.opengis.net\/sensorML\/1.0.1\n\t","type":"InsertSensor"}
```
If you wanted to get clean output from the command and pipe it through `jq` to pretty print it, you could
add the nop slf4j logging implementation to the classpath. You can't add an additional jar to the classpath
using the `-jar` notation, so you have to use the `-cp` notation (and provide the name of the class 
with the `main` method). Assuming you download the slf4j nop jar file into your current directory, the
command to pretty-print the sample and its output looks like the following:
```shell
java -cp ./slf4j-nop-1.7.32.jar:target/SWEXmlParser-1.0-SNAPSHOT-shaded.jar \
     mil.dia.swe.SWETransform src/test/resources/trailcam.xml | jq .
{
  "observableProperty": "urn:gsw:def:phenomenon:radiance\n\t",
  "metadata": {
    "observationType": "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation",
    "featureOfInterestType": "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint",
    "type": "SosInsertionMetadata"
  },
  "procedureDescription": {
    "characteristics": {
      "field": [
        {
          "uom": {
            "href": "urn:ogc:def:uom:OGC:1.0:ampHours"
          },
          "name": "Battery Amp-hours remaining",
          "type": "Quantity",
          "value": 3.6
        },
        {
          "name": "Sensor State",
          "type": "Text",
          "value": "A"
        },
        {
          "name": "Sensor String",
          "type": "Text",
          "value": "Trailcam"
        },
        {
          "name": "Inbound Channel",
          "type": "Text",
          "value": "0001"
        },
        {
          "name": "Device ID",
          "type": "Text",
          "value": "000001"
        },
        {
          "name": "Plan Name",
          "type": "Text",
          "value": "Capture Bigfoot"
        },
        {
          "name": "Plan Owner",
          "type": "Text",
          "value": "Willey Ohioman"
        },
        {
          "name": "Comments",
          "type": "Text",
          "value": "Willey Ohioman"
        },
        {
          "name": "Symbol String",
          "type": "Text",
          "value": "SFGPES------US-"
        }
      ],
      "type": "DataRecord"
    },
    "identification": {
      "identifier": [
        {
          "name": "uniqueID",
          "definition": "urn:ogc:def:identifierType:OGC:uniqueID",
          "type": "Term",
          "value": "urn:ogc:object:Sensor:Trailcam"
        },
        {
          "name": "uniqueID",
          "definition": "urn:ogc:def:identifier:OGC:1.0:uniqueID",
          "type": "Term",
          "value": "urn:ogc:object:Sensor:Trailcam"
        },
        {
          "name": "longName",
          "definition": "urn:ogc:def:identifier:OGC:1.0:longName",
          "type": "Term",
          "value": "Super Secret Trailcam"
        },
        {
          "name": "shortName",
          "definition": "urn:ogc:def:identifier:OGC:1.0:shortName",
          "type": "Term",
          "value": "Trailcam"
        }
      ],
      "type": "IdentifierList"
    },
    "keywords": {
      "type": "KeywordList",
      "keyword": [
        "Trailcam",
        "unattended",
        "ground",
        "sensors",
        "ugs"
      ]
    },
    "member": {
      "outputs": {
        "output": [
          {
            "name": "Message Type",
            "type": "Text",
            "value": ""
          },
          {
            "name": "Channel",
            "type": "Text",
            "value": ""
          },
          {
            "name": "Detail",
            "type": "Text",
            "value": ""
          },
          {
            "name": "Image",
            "type": "Text",
            "value": ""
          }
        ],
        "type": "OutputList"
      },
      "name": "Trailcam",
      "description": "An unattended ground\n\t\t\t\t\t\tsensor with sophisticaed image recognition capabilities that only\n\t\t\t\t\t\ttakes pictures of bigfoot",
      "location": {
        "srsName": "EPSG:4979",
        "srsDimension": "3",
        "pos": "39.69427 -84.10164 0.0",
        "id": "sensorLocation",
        "type": "Point"
      },
      "type": "Component",
      "sweName": "name"
    },
    "validTime": {
      "timePosition": {
        "value": "2020-01-01T00:00:01Z",
        "indeterminatePosition": "after"
      },
      "type": "TimeInstant"
    },
    "type": "SensorML"
  },
  "procedureDescriptionFormat": "http://www.opengis.net/sensorML/1.0.1\n\t",
  "type": "InsertSensor"
}
```

# ToDo:
* Fix the ordering to the recommended ordering by the best practices and the XML schemas
* Include all the options for the abstract classes - only the ones in the sample files were included
* Consider switching to a streaming parser and converting on the fly based on the rulesets
* Write actual unit tests
* Add the other SOS/SWE XML formats (InsertObservation, DescribeSensor) and the corresponding responses
