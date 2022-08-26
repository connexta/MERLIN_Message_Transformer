package mil.dia.swe;

import net.opengis.gml.v_3_1_1.LineStringType;
import net.opengis.gml.v_3_1_1.LocationPropertyType;
import net.opengis.gml.v_3_1_1.PointPropertyType;
import net.opengis.gml.v_3_1_1.PointType;
import net.opengis.gml.v_3_1_1.TimePositionType;
import net.opengis.sensorml.v_1_0_1.Capabilities;
import net.opengis.sensorml.v_1_0_1.Characteristics;
import net.opengis.sensorml.v_1_0_1.Classification;
import net.opengis.sensorml.v_1_0_1.ComponentType;
import net.opengis.sensorml.v_1_0_1.Components;
import net.opengis.sensorml.v_1_0_1.Contact;
import net.opengis.sensorml.v_1_0_1.ContactInfo;
import net.opengis.sensorml.v_1_0_1.ContactList;
import net.opengis.sensorml.v_1_0_1.Document;
import net.opengis.sensorml.v_1_0_1.DocumentList;
import net.opengis.sensorml.v_1_0_1.Documentation;
import net.opengis.sensorml.v_1_0_1.Identification;
import net.opengis.sensorml.v_1_0_1.IoComponentPropertyType;
import net.opengis.sensorml.v_1_0_1.Keywords;
import net.opengis.sensorml.v_1_0_1.LegalConstraint;
import net.opengis.sensorml.v_1_0_1.Location;
import net.opengis.sensorml.v_1_0_1.OnlineResource;
import net.opengis.sensorml.v_1_0_1.Person;
import net.opengis.sensorml.v_1_0_1.ResponsibleParty;
import net.opengis.sensorml.v_1_0_1.Rights;
import net.opengis.sensorml.v_1_0_1.Security;
import net.opengis.sensorml.v_1_0_1.SecurityConstraint;
import net.opengis.sensorml.v_1_0_1.SensorML;
import net.opengis.sensorml.v_1_0_1.SystemType;
import net.opengis.sensorml.v_1_0_1.ValidTime;
import net.opengis.sos.v_2_0.SosInsertionMetadataType;
import net.opengis.swecommon.v_1_0_1.AbstractDataArrayType;
import net.opengis.swecommon.v_1_0_1.AbstractDataRecordType;
import net.opengis.swecommon.v_1_0_1.AllowedValues;
import net.opengis.swecommon.v_1_0_1.BinaryBlock;
import net.opengis.swecommon.v_1_0_1.BlockEncodingPropertyType;
import net.opengis.swecommon.v_1_0_1.Boolean;
import net.opengis.swecommon.v_1_0_1.Category;
import net.opengis.swecommon.v_1_0_1.Count;
import net.opengis.swecommon.v_1_0_1.DataArrayType;
import net.opengis.swecommon.v_1_0_1.DataComponentPropertyType;
import net.opengis.swecommon.v_1_0_1.DataRecordType;
import net.opengis.swecommon.v_1_0_1.ObservableProperty;
import net.opengis.swecommon.v_1_0_1.Quantity;
import net.opengis.swecommon.v_1_0_1.QuantityRange;
import net.opengis.swecommon.v_1_0_1.SimpleDataRecordType;
import net.opengis.swecommon.v_1_0_1.Text;
import net.opengis.swecommon.v_1_0_1.TextBlock;
import net.opengis.swecommon.v_1_0_1.Time;
import net.opengis.swecommon.v_1_0_1.UomPropertyType;
import net.opengis.swecommon.v_1_0_1.XMLBlockType;
import net.opengis.swes.v_2_0.InsertSensorType;
import net.opengis.swes.v_2_0.InsertionMetadataType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InsertSensorTransformer {
  private static final Logger LOGGER = LoggerFactory.getLogger(InsertSensorTransformer.class);

  // Establish a single JAXBContext - they are threadsafe whereas (un)marshallers are not.
  private static JAXBContext JAXB_CONTEXT;

  static {
    try {
      JAXB_CONTEXT =
          JAXBContext.newInstance(
              "net.opengis.swes.v_2_0:net.opengis.sos.v_2_0:net.opengis.sensorml.v_1_0_1:net.opengis.swecommon.v_1_0_1");
    } catch (JAXBException e) {
      LOGGER.error("Unable to establish initial JAXBContext for InsertSensor", e);
    }
  }

  protected static String insertSensorToJSON(InputStream is) throws IllegalArgumentException {
    Unmarshaller unmarshaller = null;
    String jsonResult = null;
    try {
      LOGGER.debug("Creating the unmarshaller for InsertSensor");
      unmarshaller = JAXB_CONTEXT.createUnmarshaller();

      // without this validation handler JAXB fails silently
      unmarshaller.setEventHandler(
          new ValidationEventHandler() {
            public boolean handleEvent(ValidationEvent event) {
              throw new RuntimeException(event.getMessage(), event.getLinkedException());
            }
          });

      // parse the input stream into an InsertSensor type
      LOGGER.debug("Parsing the input XML into InsertSensor type");
      JAXBElement<InsertSensorType> insertSensorElement =
          (JAXBElement<InsertSensorType>) unmarshaller.unmarshal(is);

      /* Do we need to verify the following?
      LOGGER.debug("Verifying that the proper metadata is present");
      InsertSensorType insertSensor = insertSensorElement.getValue();
      InsertSensorType.Metadata metadata = insertSensor.getMetadata().get(0);
      if (metadata != null && insertSensor.getMetadata().size() != 1) {
        throw new IllegalArgumentException("Null or more metadata than expected"));
      }
       */
      jsonResult = toJSON(insertSensorElement.getValue());
    } catch (JAXBException e) {
      throw new RuntimeException("Exception parsing input XML", e);
    }
    return jsonResult;
  }

  private static String toJSON(InsertSensorType insertSensor) throws UnmarshalException {
    // Work through each element, converting via OGC best-practice rules for JSON
    JSONObject json = new JSONObject();
    json.put("type", "InsertSensor");
    String str = null;
    if (insertSensor == null) {
      return json.toJSONString();
    }

    // go through all the properties of the InsertSensor
    if (insertSensor.isSetProcedureDescriptionFormat()) {
      json.put("procedureDescriptionFormat", insertSensor.getProcedureDescriptionFormat());
    }

    if (insertSensor.isSetProcedureDescription()) {
      JSONObject procDesc = new JSONObject();
      Object gpd = insertSensor.getProcedureDescription().getAny();
      if (gpd instanceof SensorML) {
        SensorML sml = (SensorML) gpd;
        procDesc = processInsertSensor(sml);
      } else {
        throw new UnmarshalException(
            "Unexpected element encountered: {}", gpd.getClass().getName());
      }
      json.put("procedureDescription", procDesc);
    }

    if (insertSensor.isSetObservableProperty()) {
      json.put("observableProperty", enumListOrValue(insertSensor.getObservableProperty()));
    }

    if (insertSensor.isSetMetadata()) {
      List<InsertSensorType.Metadata> metadata = insertSensor.getMetadata();
      JSONObject metaObj = new JSONObject();
      metaObj.put("type", "SosInsertionMetadata");
      JSONArray obsTypeArray = new JSONArray();
      JSONArray foiTypeArray = new JSONArray();
      metadata.forEach(
          m -> {
            if (m.isSetInsertionMetadata()) {
              JAXBElement<? extends InsertionMetadataType> imt = m.getInsertionMetadata();
              if ("SosInsertionMetadataType".equals(imt.getDeclaredType().getSimpleName())) {
                SosInsertionMetadataType simt = (SosInsertionMetadataType) imt.getValue();
                if (simt.isSetObservationType()) {
                  // obsTypeArray.addAll(simt.getObservationType()); // this adds unexpected
                  // newlines/whitespace
                  simt.getObservationType()
                      .forEach(
                          s -> {
                            obsTypeArray.add(s.trim());
                          });
                }
                if (simt.isSetFeatureOfInterestType()) {
                  // foiTypeArray.addAll(simt.getFeatureOfInterestType()); // this adds unexpected
                  // newlines/whitespace
                  simt.getFeatureOfInterestType()
                      .forEach(
                          s -> {
                            foiTypeArray.add(s.trim());
                          });
                }
              }
            }
          });
      if (obsTypeArray.size() > 0) {
        metaObj.put(
            "observationType", obsTypeArray.size() > 1 ? obsTypeArray : obsTypeArray.get(0));
      }
      if (foiTypeArray.size() > 0) {
        metaObj.put(
            "featureOfInterestType", foiTypeArray.size() > 1 ? foiTypeArray : foiTypeArray.get(0));
      }
      json.put("metadata", metaObj);
    }
    return json.toJSONString();
  }

  private static JSONObject processInsertSensor(SensorML sensor) {
    JSONObject smlObj = new JSONObject();
    smlObj.put("type", "SensorML");

    // handle the keywords
    List<Keywords> keywords = sensor.getKeywords();
    JSONObject kws = new JSONObject();
    kws.put("type", "KeywordList");
    List<String> keywordStrings = new ArrayList<>();
    keywords.forEach(
        kw -> {
          List<String> strings = kw.getKeywordList().getKeyword();
          strings.forEach(
              thisKW -> {
                keywordStrings.add(thisKW);
              });
        });
    if (keywordStrings.size() > 1) {
      final JSONArray kwarray = new JSONArray();
      kwarray.addAll(keywordStrings);
      kws.put("keyword", kwarray);
    } else {
      if (keywordStrings.size() == 1) {
        kws.put("keyword", keywordStrings.get(0));
      } else {
        kws.put("keyword", "");
      }
    }
    smlObj.put("keywords", kws);

    // handle the identification
    List<Identification> identList = sensor.getIdentification();
    JSONObject ident = new JSONObject();
    ident.put("type", "IdentifierList");
    List<Identification.IdentifierList.Identifier> identifiers = new ArrayList<>();
    identList.forEach(
        i -> {
          for (Identification.IdentifierList.Identifier thisId :
              i.getIdentifierList().getIdentifier()) {
            identifiers.add(thisId);
          }
        });
    JSONArray identifierArray = new JSONArray();
    identifiers.forEach(
        id -> {
          JSONObject thisObj = new JSONObject();
          thisObj.put("type", "Term");
          thisObj.put("name", id.getName());
          thisObj.put("definition", id.getTerm().getDefinition());
          thisObj.put("value", id.getTerm().getValue());
          identifierArray.add(thisObj);
        });
    ident.put("identifier", identifierArray);
    smlObj.put("identification", ident);

    // handle the classification
    List<Classification> classificationList = sensor.getClassification();
    if (classificationList != null && classificationList.size() > 0) {
      Classification classification = classificationList.get(0);
      JSONObject classificationObj = new JSONObject();
      classificationObj.put("type", "ClassifierList");
      JSONArray classifierArray = new JSONArray();
      classification
          .getClassifierList()
          .getClassifier()
          .forEach(
              cl -> {
                JSONObject thisObj = new JSONObject();
                thisObj.put("type", "Term");
                thisObj.put("name", cl.getName());
                thisObj.put("definition", cl.getTerm().getDefinition());
                thisObj.put("value", cl.getTerm().getValue());
                classifierArray.add(thisObj);
              });
      classificationObj.put("classifier", classifierArray);
      smlObj.put("classification", classificationObj);
    }

    // handle the ValidTime
    ValidTime vTime = sensor.getValidTime();
    JSONObject validTimeObj = new JSONObject();
    if (vTime != null) {
      if (vTime.isSetTimeInstant()) {
        validTimeObj.put("type", "TimeInstant");
        JSONObject tposObj = new JSONObject();
        TimePositionType tp = vTime.getTimeInstant().getTimePosition();
        if (tp.isSetCalendarEraName()) {
          tposObj.put("calendarEraName", tp.getCalendarEraName());
        }
        if (tp.isSetFrame()) {
          tposObj.put("frame", tp.getFrame());
        }
        if (tp.isSetIndeterminatePosition()) {
          tposObj.put("indeterminatePosition", tp.getIndeterminatePosition().value());
        }
        if (tp.isSetValue()) {
          List<String> values = tp.getValue();
          if (values.size() > 1) {
            JSONArray valueArray = new JSONArray();
            valueArray.addAll(values);
            tposObj.put("value", valueArray);
          } else {
            tposObj.put("value", values.get(0));
          }
        }
        validTimeObj.put("timePosition", tposObj);
      } else if (vTime.isSetTimePeriod()) {
        validTimeObj.put("type", "TimePeriod");
        if (vTime.getTimePeriod().isSetBegin()) {
          validTimeObj.put(
              "begin",
              vTime
                  .getTimePeriod()
                  .getBegin()
                  .getTimeInstant()
                  .getTimePosition()
                  .getValue()
                  .get(0));
        }
        if (vTime.getTimePeriod().isSetBeginPosition()) {
          List<String> values = vTime.getTimePeriod().getBeginPosition().getValue();
          if (values.size() > 1) {
            JSONArray valueArray = new JSONArray();
            valueArray.addAll(values);
            validTimeObj.put("beginPosition", valueArray);
          } else {
            validTimeObj.put("beginPosition", values.get(0));
          }
        }
        if (vTime.getTimePeriod().isSetDuration()) {
          validTimeObj.put("duration", vTime.getTimePeriod().getDuration().toString());
        }
        if (vTime.getTimePeriod().isSetEnd()) {
          validTimeObj.put(
              "end",
              vTime.getTimePeriod().getEnd().getTimeInstant().getTimePosition().getValue().get(0));
        }
        if (vTime.getTimePeriod().isSetEndPosition()) {
          List<String> values = vTime.getTimePeriod().getEndPosition().getValue();
          if (values.size() > 1) {
            JSONArray valueArray = new JSONArray();
            valueArray.addAll(values);
            validTimeObj.put("endPosition", valueArray);
          } else {
            validTimeObj.put("endPosition", values.get(0));
          }
        }
        if (vTime.getTimePeriod().isSetTimeInterval()) {
          validTimeObj.put(
              "timeInterval", vTime.getTimePeriod().getTimeInterval().getValue().toString());
        }
      }
      smlObj.put("validTime", validTimeObj);
    }

    // handle Security Constraints
    SecurityConstraint securityConstraint = sensor.getSecurityConstraint();
    if (securityConstraint != null) {
      JSONObject scObj = new JSONObject();
      scObj.put("type", "Security");
      Security sec = securityConstraint.getSecurity();
      if (sec.isSetClassification()) {
        scObj.put("classification", sec.getClassification().value());
      }
      if (sec.isSetClassificationReason()) {
        scObj.put("classificationReason", sec.getClassificationReason());
      }
      if (sec.isSetClassifiedBy()) {
        scObj.put("classifiedBy", sec.getClassifiedBy());
      }
      if (sec.isSetDeclassDate()) {
        scObj.put("declassDate", sec.getDeclassDate());
      }
      if (sec.isSetDeclassEvent()) {
        scObj.put("declassEvent", sec.getDeclassEvent());
      }
      if (sec.isSetDateOfExemptedSource()) {
        scObj.put("dateOfExemptedSource", sec.getDateOfExemptedSource().toString());
      }
      if (sec.isSetDeclassManualReview()) {
        scObj.put("declassManualReview", sec.isDeclassManualReview());
      }
      if (sec.isSetDerivedFrom()) {
        scObj.put("derivedFrom", sec.getDerivedFrom());
      }
      if (sec.isSetDeclassException()) {
        scObj.put("declassException", enumListOrValue(sec.getDeclassException()));
      }
      if (sec.isSetDisseminationControls()) {
        scObj.put("disseminationControls", enumListOrValue(sec.getDisseminationControls()));
      }
      if (sec.isSetFGIsourceOpen()) {
        scObj.put("FGISourceOpen", enumListOrValue(sec.getFGIsourceOpen()));
      }
      if (sec.isSetFGIsourceProtected()) {
        scObj.put("FGISourceProtected", enumListOrValue(sec.getFGIsourceProtected()));
      }
      if (sec.isSetNonICmarkings()) {
        scObj.put("nonICmarkings", enumListOrValue(sec.getNonICmarkings()));
      }
      if (sec.isSetOwnerProducer()) {
        scObj.put("ownerProducer", enumListOrValue(sec.getOwnerProducer()));
      }
      if (sec.isSetReleasableTo()) {
        scObj.put("releaseableTo", enumListOrValue(sec.getReleasableTo()));
      }
      if (sec.isSetSARIdentifier()) {
        scObj.put("SARIdentifier", enumListOrValue(sec.getSARIdentifier()));
      }
      if (sec.isSetSCIcontrols()) {
        scObj.put("sciControls", enumListOrValue(sec.getSCIcontrols()));
      }
      if (sec.isSetTypeOfExemptedSource()) {
        scObj.put("typeOfExemptedSource", enumListOrValue(sec.getTypeOfExemptedSource()));
      }
      smlObj.put("securityConstraint", scObj);
    }

    // handle the legal constraints
    List<LegalConstraint> legalList = sensor.getLegalConstraint();
    if (legalList != null && legalList.size() > 0) {
      // only 1 Rights allowed
      JSONObject llObj = new JSONObject();
      llObj.put("type", "Rights");
      Rights rights = legalList.get(0).getRights();
      if (rights.isSetCopyRights()) {
        llObj.put("copyRights", rights.isCopyRights());
      }
      if (rights.isSetDocumentation()) {
        // TODO: handle all the document types
        llObj.put("documentation", "");
      }
      if (rights.isSetId()) {
        llObj.put("id", rights.getId());
      }
      if (rights.isSetPrivacyAct()) {
        llObj.put("privacyAct", rights.isPrivacyAct());
      }
      if (rights.isSetIntellectualPropertyRights()) {
        llObj.put("intellectualPropertyRights", rights.isIntellectualPropertyRights());
      }
      if (rights.isSetCopyRights()) {
        llObj.put("copyRights", rights.isCopyRights());
      }
      smlObj.put("legalConstraint", llObj);
    }

    // handle the characteristics
    List<Characteristics> cList = sensor.getCharacteristics();
    if (cList != null && cList.size() > 0) {
      //        JSONObject characteristics = new JSONObject();
      //        JSONArray fields = new JSONArray();
      //        characteristics.put("type", "DataRecord");
      Characteristics dataRecord = cList.get(0);
      JAXBElement elt = (JAXBElement<SimpleDataRecordType>) dataRecord.getAbstractDataRecord();
      LOGGER.trace(
          "ClassType for AbstractDataRecord: " + elt.getValue().getClass().getCanonicalName());
      DataRecordType dr = (DataRecordType) elt.getValue();
      JSONObject drObj = processDataRecordFields(dr);
      smlObj.put("characteristics", drObj);
    }

    // handle the capabilities
    List<Capabilities> capList = sensor.getCapabilities();
    if (capList != null && capList.size() > 0) {
      // JSONObject capabilities = new JSONObject();
      // JSONArray fields = new JSONArray();
      // capabilities.put("type", "DataRecord");
      Capabilities dataRecord = capList.get(0);
      JAXBElement elt = (JAXBElement<SimpleDataRecordType>) dataRecord.getAbstractDataRecord();
      LOGGER.trace(
          "ClassType for AbstractDataRecord: " + elt.getValue().getClass().getCanonicalName());
      DataRecordType dr = (DataRecordType) elt.getValue();
      JSONObject drObj = processDataRecordFields(dr);
      // capabilities.put("field", drObj);
      smlObj.put("capabilities", drObj);
    }

    // handle the contacts
    if (sensor.isSetContact()) {
      List<Contact> contactList = sensor.getContact();
      JSONArray cArry = new JSONArray();
      contactList.forEach(
          c -> {
            cArry.add(processContact(c));
          });
      smlObj.put("contact", cArry);
    }

    // handle the documentation
    if (sensor.isSetDocumentation()) {
      List<Documentation> docs = sensor.getDocumentation();
      JSONArray docsArray = new JSONArray();
      docs.forEach(
          d -> {
            docsArray.add(processDocumentation(d));
          });
      smlObj.put("documentation", docsArray.size() > 1 ? docsArray : docsArray.get(0));
    }

    // ignore the history for now

    // handle members
    if (sensor.isSetMember()) {
      List<SensorML.Member> members = sensor.getMember();
      JSONArray mArray = new JSONArray();
      members.forEach(
          m -> {
            mArray.add(processMember(m));
          });
      smlObj.put("member", mArray.size() > 1 ? mArray : mArray.get(0));
    }
    return smlObj;
  }

  private static JSONObject processMember(SensorML.Member m) {
    JSONObject memberObj = new JSONObject();
    String memberType = m.getProcess().getDeclaredType().getSimpleName();
    LOGGER.debug("Class inside of member: {}", m.getProcess().getDeclaredType().getCanonicalName());
    if ("ComponentType".equals(memberType)) {
      memberObj.put("type", "Component");
      ComponentType ct = (ComponentType) m.getProcess().getValue();
      processComponentType(memberObj, ct);
    } else if ("SystemType".equals(memberType)) {
      SystemType st = (SystemType) m.getProcess().getValue();
      memberObj.put("type", "System");
      if (st.isSetDescription()) {
        memberObj.put("description", st.getDescription().getValue());
      }
      if (st.isSetName()) {
        JSONArray names = new JSONArray();
        st.getName()
            .forEach(
                c -> {
                  names.add(c.getValue().getValue());
                });
        memberObj.put("name", names.size() > 1 ? names : names.get(0));
      }
      if (st.isSetLocation()) {
        JSONObject locObj = new JSONObject();
        JAXBElement<? extends LocationPropertyType> loc = st.getLocation();
        //locObj = processLocation(loc.getValue());
        if (loc.getValue().isSetLocationKeyWord()) {
          locObj.put("locationKeyword", loc.getValue().getLocationKeyWord().getValue());
        }
        if (loc.getValue().isSetLocationString()) {
          locObj.put("locationString", loc.getValue().getLocationString().getValue());
        }
        if (loc.getValue().isSetGeometry()) {
          String geoType = loc.getValue().getGeometry().getDeclaredType().getSimpleName();
          // pick a few of the most popular ones for now
          if ("PointType".equals(geoType)) {
            processPointType(locObj, (PointType) loc.getValue().getGeometry().getValue());
          } else if ("LineStringType".equals(geoType)) {
            locObj.put("type", "LineString");
            LineStringType lst = (LineStringType) loc.getValue().getGeometry().getValue();
            if (lst.isSetDescription()) {
              locObj.put("description", lst.getDescription());
            }
            if (lst.isSetPosOrPointPropertyOrPointRep()) {
              List<JAXBElement<?>> lstList = lst.getPosOrPointPropertyOrPointRep();
              lstList.forEach(
                  l -> {
                    String n = l.getDeclaredType().getClass().getSimpleName();
                    if ("PointPropertyType".equals(n)) {
                      PointPropertyType ppt = (PointPropertyType) l.getValue();
                      if (ppt.isSetPoint()) {
                        processPointType(locObj, ppt.getPoint());
                      }
                    }
                  });
            }
          }
        }
        memberObj.put("location", locObj);
      }
      if (st.isSetTargetLocation()) {
        JSONObject locObj = new JSONObject();
        Location loc = st.getTargetLocation();
        if (loc.isSetPoint()) {
          processPointType(locObj, loc.getPoint());
        }
        memberObj.put("location", locObj);
      }
      // now process the components field
      if (st.isSetComponents()) {
        List<Components.ComponentList.Component> comps =
            st.getComponents().getComponentList().getComponent();
        JSONArray cmpArray = new JSONArray();
        comps.forEach(
            c -> {
              JSONObject comp = new JSONObject();
              if (c.isSetName()) {
                comp.put("name", c.getName());
              }
              if (c.isSetHref()) {
                comp.put("href", c.getHref());
              }
              // process the Component obj and add the elements to current obj
              processComponent(comp, c);
              cmpArray.add(comp);
            });
        JSONObject components = new JSONObject();
        components.put("type", "ComponentList");
        components.put("component", cmpArray);
        memberObj.put("components", components);
      }
    }
    LOGGER.trace("Member JAXB class: " + m.getProcess().getDeclaredType().getCanonicalName());
    return memberObj;
  }

  private static JSONObject processLocation(LocationPropertyType loc) {
    JSONObject locObj = new JSONObject();
    if (loc.isSetLocationKeyWord()) {
      locObj.put("locationKeyword", loc.getLocationKeyWord().getValue());
    }
    if (loc.isSetLocationString()) {
      locObj.put("locationString", loc.getLocationString().getValue());
    }
    if (loc.isSetGeometry()) {
      String geoType = loc.getGeometry().getDeclaredType().getSimpleName();
      // pick a few of the most popular ones for now
      if ("PointType".equals(geoType)) {
        processPointType(locObj, (PointType) loc.getGeometry().getValue());
      } else if ("LineStringType".equals(geoType)) {
        locObj.put("type", "LineString");
        LineStringType lst = (LineStringType) loc.getGeometry().getValue();
        if (lst.isSetDescription()) {
          locObj.put("description", lst.getDescription());
        }
        if (lst.isSetPosOrPointPropertyOrPointRep()) {
          List<JAXBElement<?>> lstList = lst.getPosOrPointPropertyOrPointRep();
          lstList.forEach(
                  l -> {
                    String n = l.getDeclaredType().getClass().getSimpleName();
                    if ("PointPropertyType".equals(n)) {
                      PointPropertyType ppt = (PointPropertyType) l.getValue();
                      if (ppt.isSetPoint()) {
                        processPointType(locObj, ppt.getPoint());
                      }
                    }
                  });
        }
      }
    }
    return locObj;
  }

  private static JSONObject processComponentType(JSONObject o, ComponentType c) {
    o.put("type", "Component");
    if (c.isSetName()) {
      JSONArray n = new JSONArray();
      c.getName().forEach(name-> {
        n.add(name.getName().getLocalPart());
      });
      o.put("sweName", enumListOrValue(n));
    }
    LOGGER.debug("Comp class: " + c.getClass().getCanonicalName());
    String classType = c.getClass().getSimpleName();
    if ("ComponentType".equals(classType)) {
      if (c.isSetDescription()) {
        o.put("description", c.getDescription().getValue());
      }
      if (c.isSetName()) {
        JSONArray names = new JSONArray();
        c.getName()
                .forEach(
                        n -> {
                          names.add(n.getValue().getValue());
                        });
        o.put("name", names.size() > 1 ? names : names.get(0));
      }
      if (c.isSetTargetLocation()) {
        Location l = c.getTargetLocation();
      }
      if (c.isSetTargetLocation()) {
        JSONObject locObj = new JSONObject();
        Location loc = c.getTargetLocation();
        if (loc.isSetPoint()) {
          processPointType(locObj, loc.getPoint());
        }
        o.put("location", locObj);
      }
      if (c.isSetLocation()) {
        LocationPropertyType lpt = c.getLocation().getValue();
        o.put("location", processLocation(lpt));
      }
      if (c.isSetInputs()) {
        JSONObject inputsObj = new JSONObject();
        inputsObj.put("type", "InputList");
        List<IoComponentPropertyType> inputs = c.getInputs().getInputList().getInput();
        JSONArray iArray = new JSONArray();
        inputs.forEach(
                i -> {
                  JSONObject input = new JSONObject();
                  if (i.isSetName()) {
                    input.put("name", i.getName());
                  }
                  if (i.isSetHref()) {
                    input.put("href", i.getHref());
                  }
                  if (i.isSetObservableProperty()) {
                    input.put("type", "ObservableProperty");
                    ObservableProperty op = i.getObservableProperty();
                    if (op.isSetDefinition()) {
                      input.put("definition", op.getDefinition());
                    }
                    if (op.isSetDescription()) {
                      input.put("description", op.getDescription().getValue());
                    }
                    if (op.isSetFixed()) {
                      input.put("fixed", op.isFixed());
                    }
                  }
                  iArray.add(input);
                });
        inputsObj.put("input", iArray);
        o.put("inputs", inputsObj);
      }
      if (c.isSetOutputs()) {
        JSONObject outputsObj = new JSONObject();
        outputsObj.put("type", "OutputList");
        List<IoComponentPropertyType> outputs = c.getOutputs().getOutputList().getOutput();
        JSONArray oArray = new JSONArray();
        outputs.forEach(
                out -> {
                  JSONObject output = new JSONObject();
                  if (out.isSetName()) {
                    output.put("name", out.getName());
                  }
                  if (out.isSetHref()) {
                    output.put("href", out.getHref());
                  }
                  if (out.isSetText()) {
                    output.put("type", "Text");
                    if (out.getText().isSetDefinition()) {
                      output.put("definition", out.getText().getDefinition());
                    }
                    if (out.getText().isSetValue()) {
                      output.put("value", out.getText().getValue());
                    }
                  }
                  if (out.isSetCategory()) {
                    output.put("type", "Category");
                    if (out.getCategory().isSetDefinition()) {
                      output.put("definition", out.getCategory().getDefinition());
                    }
                    if (out.getCategory().isSetValue()) {
                      output.put("value", out.getCategory().getValue());
                    }
                  }
                  if (out.isSetQuantity()) {
                    output.put("type", "Quantity");
                    Quantity q = out.getQuantity();
                    if (q.isSetDefinition()) {
                      output.put("definition", q.getDefinition());
                    }
                    if (q.isSetUom()) {
                      output.put("uom", processUOMPropertyType(q.getUom()));
                    }
                    if (q.isSetValue()) {
                      output.put("value", out.getCategory().getValue());
                    }
                  }
                  if (out.isSetAbstractDataArray()) {
                    JAXBElement<AbstractDataArrayType> da = out.getAbstractDataArray();
                    AbstractDataArrayType adat = da.getValue();
                    String className = adat.getClass().getSimpleName();
                    if ("DataArray".equals(className)) {
                      DataArrayType dat = (DataArrayType) adat;
                      output.put("type", "DataArray");
                      if (adat.isSetDefinition()) {
                        output.put("definition", adat.getDefinition());
                      }
                      if (adat.isSetElementCount()) {
                        output.put(
                                "elementCount", adat.getElementCount().getCount().getValue().toString());
                      }
                      if (dat.isSetElementType()) {
                        DataComponentPropertyType dcpt = dat.getElementType();
                        JSONObject elementTypeObj = new JSONObject();
                        if (dcpt.isSetName()) {
                          elementTypeObj.put("name", dcpt.getName());
                        }
                        if (dcpt.isSetAbstractDataRecord()) {
                          AbstractDataRecordType adrt =
                                  (AbstractDataRecordType) dcpt.getAbstractDataRecord().getValue();
                          String adrtClass = adrt.getClass().getSimpleName();
                          if ("DataRecord".equals(adrtClass)) {
                            elementTypeObj.put("type", "DataRecord");
                            DataRecordType drt = (DataRecordType) adrt;
                            if (drt.isSetField()) {
                              JSONArray fields = new JSONArray();
                              drt.getField()
                                      .forEach(
                                              props -> {
                                                JSONObject fObj = new JSONObject();
                                                if (props.isSetName()) {
                                                  fObj.put("name", props.getName());
                                                }
                                                if (props.isSetCount()) {
                                                  fObj.put("count", props.getCount().getValue().toString());
                                                }
                                                if (props.isSetQuantity()) {
                                                  fObj.put("quantity", props.getQuantity().getValue());
                                                }
                                                if (props.isSetTime()) {
                                                  fObj.put("time", enumListOrValue(props.getTime().getValue()));
                                                }
                                                if (props.isSetCategory()) {
                                                  fObj.put("type", "Category");
                                                  fObj.put("definition", props.getCategory().getValue());
                                                }
                                                if (props.isSetText()) {
                                                  fObj.put("type", "Text");
                                                  fObj.put("definition", props.getText().getValue());
                                                }
                                                if (props.isSetQuantityRange()) {
                                                  fObj.put("type", "QuantityRange");
                                                  QuantityRange qr = props.getQuantityRange();
                                                  List<Double> doubles = qr.getValue();
                                                  if (qr.isSetDefinition()) {
                                                    fObj.put("definition", qr.getDefinition());
                                                  }
                                                  if (qr.isSetUom()) {
                                                    fObj.put("uom", processUOMPropertyType(qr.getUom()));
                                                  }
                                                  if (qr.isSetConstraint()) {
                                                    if (qr.getConstraint().isSetAllowedValues()) {
                                                      JSONObject avObj = new JSONObject();
                                                      AllowedValues av = qr.getConstraint().getAllowedValues();
                                                      if (av.isSetMin()) {
                                                        avObj.put("min", av.getMin());
                                                      }
                                                      if (av.isSetMax()) {
                                                        avObj.put("max", av.getMax());
                                                      }
                                                      if (av.isSetIntervalOrValueList()) {
                                                        List<Double> avdoubles =
                                                                av.getIntervalOrValueList().get(0).getValue();
                                                        StringBuilder sb = new StringBuilder();
                                                        avdoubles.forEach(
                                                                d -> {
                                                                  sb.append(d);
                                                                  sb.append(" ");
                                                                });
                                                        // TODO: this is sketchy - didn't have exmaples to
                                                        // follow
                                                        avObj.put(
                                                                av.getClass().getSimpleName(),
                                                                sb.toString().trim());
                                                      }
                                                      fObj.put("constraint", avObj);
                                                    }
                                                  }
                                                  if (qr.isSetValue()) {
                                                    fObj.put("value", qr.getValue());
                                                  }
                                                }
                                              });
                            }
                          }
                        }
                        elementTypeObj.put("type", "DataRecord");
                        if (dcpt.isSetCount()) {
                          elementTypeObj.put("count", dcpt.getCount().getValue().toString());
                        }
                        if (dcpt.isSetQuantity()) {}
                      }
                    }
                  }
                  oArray.add(output);
                });
        outputsObj.put("output", oArray);
        o.put("outputs", outputsObj);
      }
    }
    return o;
  }


  private static JSONObject processComponent(JSONObject o, Components.ComponentList.Component c) {
    o.put("type", "Component");
    if (c.isSetName()) {
      o.put("sweName", c.getName());
    }
    LOGGER.debug("Comp class: " + c.getProcess().getValue().getClass().getCanonicalName());
    String classType = c.getProcess().getValue().getClass().getSimpleName();
    if ("ComponentType".equals(classType)) {
      ComponentType ct = (ComponentType) c.getProcess().getValue();
      if (ct.isSetDescription()) {
        o.put("description", ct.getDescription().getValue());
      }
      if (ct.isSetName()) {
        JSONArray names = new JSONArray();
        ct.getName()
            .forEach(
                n -> {
                  names.add(n.getValue().getValue());
                });
        o.put("name", names.size() > 1 ? names : names.get(0));
      }
      if (ct.isSetInputs()) {
        JSONObject inputsObj = new JSONObject();
        inputsObj.put("type", "InputList");
        List<IoComponentPropertyType> inputs = ct.getInputs().getInputList().getInput();
        JSONArray iArray = new JSONArray();
        inputs.forEach(
            i -> {
              JSONObject input = new JSONObject();
              if (i.isSetName()) {
                input.put("name", i.getName());
              }
              if (i.isSetHref()) {
                input.put("href", i.getHref());
              }
              if (i.isSetObservableProperty()) {
                input.put("type", "ObservableProperty");
                ObservableProperty op = i.getObservableProperty();
                if (op.isSetDefinition()) {
                  input.put("definition", op.getDefinition());
                }
                if (op.isSetDescription()) {
                  input.put("description", op.getDescription().getValue());
                }
                if (op.isSetFixed()) {
                  input.put("fixed", op.isFixed());
                }
              }
              iArray.add(input);
            });
        inputsObj.put("input", iArray);
        o.put("inputs", inputsObj);
      }
      if (ct.isSetOutputs()) {
        JSONObject outputsObj = new JSONObject();
        outputsObj.put("type", "OutputList");
        List<IoComponentPropertyType> outputs = ct.getOutputs().getOutputList().getOutput();
        JSONArray oArray = new JSONArray();
        outputs.forEach(
            out -> {
              JSONObject output = new JSONObject();
              if (out.isSetName()) {
                output.put("name", out.getName());
              }
              if (out.isSetHref()) {
                output.put("href", out.getHref());
              }
              if (out.isSetText()) {
                output.put("type", "Text");
                if (out.getText().isSetDefinition()) {
                  output.put("definition", out.getText().getDefinition());
                }
                if (out.getText().isSetValue()) {
                  output.put("value", out.getText().getValue());
                }
              }
              if (out.isSetCategory()) {
                output.put("type", "Category");
                if (out.getCategory().isSetDefinition()) {
                  output.put("definition", out.getCategory().getDefinition());
                }
                if (out.getCategory().isSetValue()) {
                  output.put("value", out.getCategory().getValue());
                }
              }
              if (out.isSetQuantity()) {
                output.put("type", "Quantity");
                Quantity q = out.getQuantity();
                if (q.isSetDefinition()) {
                  output.put("definition", q.getDefinition());
                }
                if (q.isSetUom()) {
                  output.put("uom", processUOMPropertyType(q.getUom()));
                }
                if (q.isSetValue()) {
                  output.put("value", out.getCategory().getValue());
                }
              }
              if (out.isSetAbstractDataArray()) {
                JAXBElement<AbstractDataArrayType> da = out.getAbstractDataArray();
                AbstractDataArrayType adat = da.getValue();
                String className = adat.getClass().getSimpleName();
                if ("DataArray".equals(className)) {
                  DataArrayType dat = (DataArrayType) adat;
                  output.put("type", "DataArray");
                  if (adat.isSetDefinition()) {
                    output.put("definition", adat.getDefinition());
                  }
                  if (adat.isSetElementCount()) {
                    output.put(
                        "elementCount", adat.getElementCount().getCount().getValue().toString());
                  }
                  if (dat.isSetElementType()) {
                    DataComponentPropertyType dcpt = dat.getElementType();
                    JSONObject elementTypeObj = new JSONObject();
                    if (dcpt.isSetName()) {
                      elementTypeObj.put("name", dcpt.getName());
                    }
                    if (dcpt.isSetAbstractDataRecord()) {
                      AbstractDataRecordType adrt =
                          (AbstractDataRecordType) dcpt.getAbstractDataRecord().getValue();
                      String adrtClass = adrt.getClass().getSimpleName();
                      if ("DataRecord".equals(adrtClass)) {
                        elementTypeObj.put("type", "DataRecord");
                        DataRecordType drt = (DataRecordType) adrt;
                        if (drt.isSetField()) {
                          JSONArray fields = new JSONArray();
                          drt.getField()
                              .forEach(
                                  props -> {
                                    JSONObject fObj = new JSONObject();
                                    if (props.isSetName()) {
                                      fObj.put("name", props.getName());
                                    }
                                    if (props.isSetCount()) {
                                      fObj.put("count", props.getCount().getValue().toString());
                                    }
                                    if (props.isSetQuantity()) {
                                      fObj.put("quantity", props.getQuantity().getValue());
                                    }
                                    if (props.isSetTime()) {
                                      fObj.put("time", enumListOrValue(props.getTime().getValue()));
                                    }
                                    if (props.isSetCategory()) {
                                      fObj.put("type", "Category");
                                      fObj.put("definition", props.getCategory().getValue());
                                    }
                                    if (props.isSetText()) {
                                      fObj.put("type", "Text");
                                      fObj.put("definition", props.getText().getValue());
                                    }
                                    if (props.isSetQuantityRange()) {
                                      fObj.put("type", "QuantityRange");
                                      QuantityRange qr = props.getQuantityRange();
                                      List<Double> doubles = qr.getValue();
                                      if (qr.isSetDefinition()) {
                                        fObj.put("definition", qr.getDefinition());
                                      }
                                      if (qr.isSetUom()) {
                                        fObj.put("uom", processUOMPropertyType(qr.getUom()));
                                      }
                                      if (qr.isSetConstraint()) {
                                        if (qr.getConstraint().isSetAllowedValues()) {
                                          JSONObject avObj = new JSONObject();
                                          AllowedValues av = qr.getConstraint().getAllowedValues();
                                          if (av.isSetMin()) {
                                            avObj.put("min", av.getMin());
                                          }
                                          if (av.isSetMax()) {
                                            avObj.put("max", av.getMax());
                                          }
                                          if (av.isSetIntervalOrValueList()) {
                                            List<Double> avdoubles =
                                                av.getIntervalOrValueList().get(0).getValue();
                                            StringBuilder sb = new StringBuilder();
                                            avdoubles.forEach(
                                                d -> {
                                                  sb.append(d);
                                                  sb.append(" ");
                                                });
                                            // TODO: this is sketchy - didn't have exmaples to
                                            // follow
                                            avObj.put(
                                                av.getClass().getSimpleName(),
                                                sb.toString().trim());
                                          }
                                          fObj.put("constraint", avObj);
                                        }
                                      }
                                      if (qr.isSetValue()) {
                                        fObj.put("value", qr.getValue());
                                      }
                                    }
                                  });
                        }
                      }
                    }
                    elementTypeObj.put("type", "DataRecord");
                    if (dcpt.isSetCount()) {
                      elementTypeObj.put("count", dcpt.getCount().getValue().toString());
                    }
                    if (dcpt.isSetQuantity()) {}
                  }
                }
              }
              oArray.add(output);
            });
        outputsObj.put("output", oArray);
        o.put("outputs", outputsObj);
      }
    }
    return o;
  }

  private static JSONObject processUOMPropertyType(UomPropertyType uom) {
    JSONObject uomObj = new JSONObject();
    if (uom != null) {
      if (uom.isSetCode()) {
        uomObj.put("code", uom.getCode());
      }
      if (uom.isSetHref()) {
        uomObj.put("href", uom.getHref());
      }
      if (uom.isSetUnitDefinition()) {
        uomObj.put("unitDefinition", uom.getUnitDefinition().getValue().toString());
      }
    }
    return uomObj;
  }

  private static JSONObject processPointType(JSONObject o, PointType pt) {
    o.put("type", "Point");
    if (pt.isSetDescription()) {
      o.put("description", pt.getDescription().getValue());
    }
    if (pt.isSetPos()) {
      List<Double> points = pt.getPos().getValue();
      StringBuffer sb = new StringBuffer();
      points.forEach(
          d -> {
            sb.append(d);
            sb.append(" ");
          });
      o.put("pos", sb.toString().trim());
    }
    if (pt.isSetSrsName()) {
      o.put("srsName", pt.getSrsName());
    }
    if (pt.isSetSrsDimension()) {
      o.put("srsDimension", pt.getSrsDimension().toString());
    }
    if (pt.isSetId()) {
      o.put("id", pt.getId());
    }
    return o;
  }

  private static JSONObject processContact(Contact c) {
    JSONObject emptyObj = new JSONObject();
    if (c.isSetResponsibleParty()) {
      ResponsibleParty rp = c.getResponsibleParty();
      JSONObject contactObj = processResponsibleParty(rp);
      contactObj.put("type", "ResponsibleParty");
      return contactObj;
    }
    if (c.isSetContactList()) {
      ContactList cl = c.getContactList();
      JSONObject clObj = new JSONObject();
      clObj.put("type", "ContactList");
      if (cl.isSetDescription()) {
        clObj.put("description", cl.getDescription().getValue());
      }
      if (cl.isSetMember()) {
        List<ContactList.Member> members = cl.getMember();
        if (members != null && members.size() > 1) {
          JSONArray membersArray = new JSONArray();
          members.forEach(
              m -> {
                if (m.isSetResponsibleParty()) {
                  membersArray.add(processResponsibleParty(m.getResponsibleParty()));
                } else if (m.isSetPerson()) {
                  membersArray.add(processPerson(m.getPerson()));
                }
              });
          clObj.put("member", membersArray);
        } else {
          ContactList.Member m = members.get(0);
          if (m.isSetResponsibleParty()) {
            JSONObject membersObj = processResponsibleParty(members.get(0).getResponsibleParty());
            clObj.put("member", membersObj);
          } else {
            clObj.put("member", processPerson(m.getPerson()));
          }
        }
      }
      return clObj;
    }
    return emptyObj;
  }

  private static JSONObject processDocument(Document d) {
    JSONObject o = new JSONObject();
    if (d.isSetDescription()) {
      o.put("description", d.getDescription().getValue());
    }
    if (d.isSetDate()) {
      o.put("date", d.getDate());
    }
    if (d.isSetContact()) {
      o.put("contact", processContact(d.getContact()));
    }
    if (d.isSetFormat()) {
      o.put("format", d.getFormat());
    }
    if (d.isSetOnlineResource()) {
      List<OnlineResource> resources = d.getOnlineResource();
      JSONArray rArray = new JSONArray();
      resources.forEach(
          olr -> {
            JSONObject olro = new JSONObject();
            if (olr.isSetHref()) {
              olro.put("href", olr.getHref());
            }
            rArray.add(olro);
          });
      o.put("onlineResource", rArray);
      o.put("onlineResource", rArray.size() > 1 ? rArray : rArray.get(0));
    }
    return o;
  }

  private static JSONObject processDocumentList(DocumentList dl) {
    JSONObject o = new JSONObject();
    o.put("type", "DocumentList");
    if (dl.isSetDescription()) {
      o.put("description", dl.getDescription().getValue());
    }
    if (dl.isSetMember()) {
      List<DocumentList.Member> members = dl.getMember();
      if (members.size() > 1) {
        JSONArray mArray = new JSONArray();
        members.forEach(
            m -> {
              mArray.add(processDocument(m.getDocument()));
            });
        o.put("member", mArray);
      } else {
        o.put("member", processDocument(members.get(0).getDocument()));
      }
    }
    return o;
  }

  private static JSONObject processDocumentation(Documentation d) {
    JSONObject o = new JSONObject();
    if (d.isSetDocument()) {
      JSONObject dObj = processDocument(d.getDocument());
      o.put("document", dObj);
    }
    if (d.isSetDocumentList()) {
      JSONObject dObj = processDocumentList(d.getDocumentList());
      o.put("documentList", dObj);
    }
    return o;
  }

  private static JSONObject processPerson(Person p) {
    JSONObject obj = new JSONObject();
    if (p.isSetName()) {
      obj.put("name", p.getName());
    }
    if (p.isSetSurname()) {
      obj.put("surname", p.getSurname());
    }
    if (p.isSetAffiliation()) {
      obj.put("affiliation", p.getAffiliation());
    }
    if (p.isSetEmail()) {
      obj.put("email", p.getEmail());
    }
    if (p.isSetPhoneNumber()) {
      obj.put("phoneNumber", p.getPhoneNumber());
    }
    if (p.isSetUserID()) {
      obj.put("userID", p.getUserID());
    }
    return obj;
  }

  private static JSONObject processResponsibleParty(ResponsibleParty rp) {
    JSONObject contactObj = new JSONObject();
    if (rp.isSetIndividualName()) {
      contactObj.put("individualName", rp.getIndividualName());
    }
    if (rp.isSetOrganizationName()) {
      contactObj.put("organizationName", rp.getOrganizationName());
    }
    if (rp.isSetPositionName()) {
      contactObj.put("positionName", rp.getPositionName());
    }
    if (rp.isSetContactInfo()) {
      ContactInfo ci = rp.getContactInfo();
      JSONObject ciObj = new JSONObject();
      if (ci.isSetPhone()) {
        ContactInfo.Phone ph = ci.getPhone();
        JSONObject phObj = new JSONObject();
        if (ph.isSetVoice()) {
          List<String> vList = ph.getVoice();
          if (vList.size() > 1) {
            JSONArray vArray = new JSONArray();
            vArray.addAll(vList);
            phObj.put("voice", vArray);
          } else {
            phObj.put("voice", vList.get(0));
          }
        }
        ciObj.put("phone", phObj);
      }
      if (ci.isSetAddress()) {
        ContactInfo.Address addr = ci.getAddress();
        JSONObject addrObj = new JSONObject();
        if (addr.isSetElectronicMailAddress()) {
          addrObj.put("electronicMailAddress", addr.getElectronicMailAddress());
        }
        if (addr.isSetAdministrativeArea()) {
          addrObj.put("administrativeArea", addr.getAdministrativeArea());
        }
        if (addr.isSetDeliveryPoint()) {
          addrObj.put("deliveryPoint", new JSONArray().addAll(addr.getDeliveryPoint()));
        }
        if (addr.isSetCity()) {
          addrObj.put("city", addr.getCity());
        }
        if (addr.isSetPostalCode()) {
          addrObj.put("postalCode", addr.getPostalCode());
        }
        if (addr.isSetCountry()) {
          addrObj.put("country", addr.getCountry());
        }
        ciObj.put("address", addrObj);
      }
      if (ci.isSetContactInstructions()) {
        ciObj.put("contactInstructions", ci.getContactInstructions());
      }
      if (ci.isSetHoursOfService()) {
        ciObj.put("hoursOfService", ci.getHoursOfService());
      }
      if (ci.isSetOnlineResource()) {
        List<OnlineResource> onlineList = ci.getOnlineResource();
        if (onlineList.size() > 1) {
          JSONArray onlineArray = new JSONArray();
          onlineList.forEach(
              href -> {
                onlineArray.add(href.getHref());
              });
          ciObj.put("onlineResource", onlineArray);
        } else {
          ciObj.put("onlineResource", onlineList.get(0).getHref());
        }
      }
      contactObj.put("contactInfo", ciObj);
    }
    return contactObj;
  }

  private static JSONObject processDataArray(DataArrayType da) {
    JSONObject obj = new JSONObject();
    obj.put("type", "DataArray");
    if (da.isSetDefinition()) {
      obj.put("definition", da.getDefinition());
    }
    if (da.isSetDescription()) {
      obj.put("description", da.getDescription().getValue());
    }
    if (da.isSetElementCount()) {
      AbstractDataArrayType.ElementCount ec = da.getElementCount();
      if (ec.isSetCount()) {
        Count count = ec.getCount();
        JSONObject countObj = new JSONObject();
        countObj.put("type", "Count");
        if (count.isSetDefinition()) {
          countObj.put("definition", count.getDefinition());
        }
        if (count.isSetValue()) {
          countObj.put("value", count.getValue().toString());
        }
        obj.put("elementCount", countObj);
      }
    }
    if (da.isSetElementType()) {
      JSONObject eltTypeObj = new JSONObject();
      if (da.getElementType().isSetAbstractDataRecord()) {
        JAXBElement elt =
            (JAXBElement<SimpleDataRecordType>) da.getElementType().getAbstractDataRecord();
        DataRecordType newDr = (DataRecordType) elt.getValue();
        eltTypeObj = processDataRecordFields(newDr);
        obj.put("elementType", eltTypeObj);
      } else if (da.getElementType().isSetAbstractDataArray()) {
        JAXBElement elt = (JAXBElement<DataArrayType>) da.getElementType().getAbstractDataArray();
        DataArrayType newDa = (DataArrayType) elt.getValue();
        eltTypeObj = processDataArray(newDa);
        obj.put("elementType", eltTypeObj);
      }
      // TODO: Add in all the other types besides abstract types here

      if (da.isSetEncoding()) {
        BlockEncodingPropertyType bept = da.getEncoding();
        JSONObject o = new JSONObject();
        if (bept.isSetTextBlock()) {
          TextBlock tb = bept.getTextBlock();
          o.put("type", "TextBlock");
          if (tb.isSetTokenSeparator()) {
            o.put("tokenSeparator", tb.getTokenSeparator());
          }
          if (tb.isSetBlockSeparator()) {
            o.put("blockSeparator", tb.getBlockSeparator());
          }
          if (tb.isSetDecimalSeparator()) {
            o.put("decimalSeparator", tb.getDecimalSeparator());
          }
        } else if (bept.isSetBinaryBlock()) {
          BinaryBlock bb = bept.getBinaryBlock();
          o.put("type", "BinaryBlock");
          if (bb.isSetByteEncoding()) {
            o.put("byteEncoding", bb.getByteEncoding().value());
          }
          if (bb.isSetByteLength()) {
            o.put("byteLength", bb.getByteLength().toString());
          }
          if (bb.isSetByteOrder()) {
            o.put("byteOrder", bb.getByteOrder().value());
          }
        } else if (bept.isSetXMLBlock()) {
          XMLBlockType xb = bept.getXMLBlock();
          o.put("type", "XMLBlockType");
          if (xb.isSetXmlElement()) {
            o.put("xmlElement", xb.getXmlElement().toString());
          }
        }
        obj.put("encoding", o);
      }
    }
    return obj;
  }

  private static JSONObject processDataRecordFields(DataRecordType dr) {
    JSONObject obj = new JSONObject();
    obj.put("type", "DataRecord");
    if (dr.isSetDefinition()) {
      obj.put("definition", dr.getDefinition());
    }
    if (dr.isSetDescription()) {
      obj.put("description", dr.getDescription().getValue());
    }
    JSONArray arry = new JSONArray();
    if (dr.isSetField()) {
      dr.getField()
          .forEach(
              f -> {
                JSONObject field = new JSONObject();

                // TODO: Check for DataArray - we're missing all subfields from a nested DataArray
                if (f.isSetAbstractDataArray()) {
                  JAXBElement elt = (JAXBElement<DataArrayType>) f.getAbstractDataArray();
                  DataArrayType dat = (DataArrayType) elt.getValue();
                  field = processDataArray(dat);
                }
                if (f.isSetAbstractDataRecord()) {
                  JAXBElement elt = (JAXBElement<SimpleDataRecordType>) f.getAbstractDataRecord();
                  DataRecordType newDr = (DataRecordType) elt.getValue();
                  field = processDataRecordFields(newDr);
                } else if (f.isSetActuate()) {
                  field.put("type", "Actuate");
                  // ???
                } else if (f.isSetQuantity()) {
                  field.put("type", "Quantity");
                  Quantity q = f.getQuantity();
                  if (q.isSetDefinition()) {
                    field.put("definition", q.getDefinition());
                  }
                  if (q.isSetUom()) {
                    field.put("uom", processUOMPropertyType(q.getUom()));
                  }
                  if (q.isSetValue()) {
                    field.put("value", q.getValue());
                  }
                } else if (f.isSetQuantityRange()) {
                  field.put("type", "QuantityRange");
                  QuantityRange qr = f.getQuantityRange();
                  if (qr.isSetDefinition()) {
                    field.put("definition", qr.getDefinition());
                  }
                  if (qr.isSetUom()) {
                    field.put("uom", processUOMPropertyType(qr.getUom()));
                  }
                  /*
                  UomPropertyType uom = qr.getUom();
                  if (uom != null) {
                    JSONObject uomObj = new JSONObject();
                    if (uom.isSetCode()) {
                      uomObj.put("code", uom.getCode());
                    }
                    if (uom.isSetHref()) {
                      uomObj.put("href", uom.getHref());
                    }
                    if (uom.isSetUnitDefinition()) {
                      uomObj.put("unitDefinition", uom.getUnitDefinition().getValue().toString());
                    }
                    field.put("uom", uomObj);
                  }
                   */
                  if (qr.isSetValue()) {
                    field.put(
                        "value",
                        String.format("%s %s", qr.getValue().get(0), qr.getValue().get(1)));
                  }
                } else if (f.isSetBoolean()) {
                  Boolean b = f.getBoolean();
                  field.put("type", "Boolean");
                  if (b.isSetDefinition()) {
                    field.put("definition", b.getDefinition());
                  }
                  if (b.isSetValue()) {
                    field.put("value", b.isValue());
                  }
                } else if (f.isSetCategory()) {
                  Category c = f.getCategory();
                  field.put("type", "Category");
                  if (c.isSetDefinition()) {
                    field.put("definition", c.getDefinition());
                  }
                  if (c.isSetValue()) {
                    field.put("value", f.getCategory().getValue());
                  }
                } else if (f.isSetCount()) {
                  Count c = f.getCount();
                  field.put("type", "Count");
                  if (c.isSetDefinition()) {
                    field.put("definition", c.getDefinition());
                  }
                  if (c.isSetValue()) {
                    field.put("value", c.getValue());
                  }
                } else if (f.isSetText()) {
                  Text t = f.getText();
                  field.put("type", "Text");
                  if (t.isSetDefinition()) {
                    field.put("definition", t.getDefinition());
                  }
                  if (t.isSetValue()) {
                    field.put("value", f.getText().getValue());
                  }
                } else if (f.isSetTime()) {
                  Time t = f.getTime();
                  field.put("type", "Time");
                  if (t.isSetDefinition()) {
                    field.put("definition", t.getDefinition());
                  }
                  if (t.isSetValue()) {
                    field.put("value", t.getValue());
                  }
                }
                if (f.isSetName()) {
                  field.put("name", f.getName());
                }
                arry.add(field);
              });
      obj.put("field", arry);
    }
    return obj;
  }

  private static Object enumListOrValue(List<String> list) {
    if (list == null) {
      return "";
    }
    if (list != null && list.size() > 1) {
      JSONArray array = new JSONArray();
      array.addAll(list);
      return array;
    }
    return list.get(0);
  }
}
