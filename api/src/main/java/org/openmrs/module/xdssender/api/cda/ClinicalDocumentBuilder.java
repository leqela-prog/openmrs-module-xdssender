package org.openmrs.module.xdssender.api.cda;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.xdssender.XdsSenderConstants;
import org.openmrs.module.xdssender.api.cda.model.DocumentModel;
import org.openmrs.module.xdssender.api.cda.obs.ExtendedObs;
import org.openmrs.module.xdssender.api.cda.section.impl.ActiveProblemsSectionBuilder;
import org.openmrs.module.xdssender.api.cda.section.impl.AntepartumFlowsheetPanelSectionBuilder;
import org.openmrs.module.xdssender.api.cda.section.impl.CodedResultsSectionBuilder;
import org.openmrs.module.xdssender.api.cda.section.impl.EstimatedDeliveryDateSectionBuilder;
import org.openmrs.module.xdssender.api.cda.section.impl.MedicationsSectionBuilder;
import org.openmrs.module.xdssender.api.cda.section.impl.VitalSignsSectionBuilder;
import org.openmrs.module.xdssender.api.everest.EverestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("xdsender.ClinicalDocumentBuilder")
public class ClinicalDocumentBuilder {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private CdaMetadataUtil metadataUtil;
	
	@Autowired
	private EstimatedDeliveryDateSectionBuilder eddSectionBuilder;
	
	@Autowired
	private AntepartumFlowsheetPanelSectionBuilder flowsheetSectionBuilder;
	
	@Autowired
	private VitalSignsSectionBuilder vitalSignsSectionBuilder;

	@Autowired
	private CodedResultsSectionBuilder codedResultsSectionBuilder;
	
	@Autowired
	private MedicationsSectionBuilder medSectionBuilder;
	
	@Autowired
	private ActiveProblemsSectionBuilder probBuilder;
	
	public DocumentModel buildDocument(Patient patient, Encounter encounter) throws InstantiationException,
	        IllegalAccessException {
		
		DocumentBuilder builder = new DocumentBuilderImpl();
		// DocumentBuilder builder = new MedicalDocumentBuilderImpl();
		builder.setRecordTarget(patient);
		builder.setEncounterEvent(encounter);
		
		Obs estimatedDeliveryDateObs = null, lastMenstrualPeriodObs = null, prepregnancyWeightObs = null,
				gestgationalAgeObs = null, fundalHeightObs = null, systolicBpObs = null, diastolicBpObs = null,
				weightObs = null, heightObs = null, presentationObs = null, temperatureObs = null;

		List<Obs> medicationObs = new ArrayList<Obs>();

		// TODO: REMEMBER TO INCLUDE WHO STAGING AND T STAGING
		// Include HIV Sentinel events observations
		Obs firstHIVPosTestObs = null, secondHIVPosRetestObs = null, artStartDate = null, artStartRegimenObs = null,
				baselineCD4Count = null, currentCD4Count = null, currentARVRegimenObs = null, hivViralLoadObs = null,
				pregnancyStatusObs = null, transferInObs = null, transferInSiteObs = null, transferOutObs = null,
				artFollowUpDateObs = null;


		// Use the Encounter types to determine what observations to save to the HIE, better aligned to defined clinical
		// workflows or business processes
		Boolean hivIntakeForm = false, hivIntakeCounselorForm = false, hivFollowUpForm = false, viralLoadMonitoringForm = false, htsForm = false, htsRetestingForm = false, nutritionAssessmentForm = false,
				vitalsForm = false;

		for (Obs obs : encounter.getObs()) {

			// Use the Encounter types to determine which data elements to send to the HIE - Key Events
			String rootConceptName = getRootConceptName(obs);

			if (rootConceptName.equalsIgnoreCase("Vitals")) {
				vitalsForm = true;
				if (obs.getConcept().getName().toString().equalsIgnoreCase("WEIGHT")) {
					weightObs = new Obs();
					weightObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("HEIGHT")) {
					heightObs = new Obs();
					heightObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("Temperature")) {
					temperatureObs = new Obs();
					temperatureObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("Systolic")) {
					systolicBpObs = new Obs();
					systolicBpObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("Diastolic")) {
					diastolicBpObs = new Obs();
					diastolicBpObs = obs;
				}
			}

			if (rootConceptName.equalsIgnoreCase("Nutritional Values")) {
				nutritionAssessmentForm = true;
				if (obs.getConcept().getName().toString().equalsIgnoreCase("WEIGHT")) {
					weightObs = new Obs();
					weightObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("HEIGHT")) {
					heightObs = new Obs();
					heightObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("Systolic")) {
					systolicBpObs = new Obs();
					systolicBpObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("Diastolic")) {
					diastolicBpObs = new Obs();
					diastolicBpObs = obs;
				}
			}

			if (rootConceptName.equalsIgnoreCase("HIV Testing and Counseling Intake Template")) {
				htsForm = true;
				if (obs.getConcept().getName().toString().equalsIgnoreCase("HTC, Final HIV status")) {
					firstHIVPosTestObs = new Obs();
					firstHIVPosTestObs = obs;
				}
			}

			if (rootConceptName.equalsIgnoreCase("HIV Testing Services Retesting Template")) {
				htsRetestingForm = true;
				if (obs.getConcept().getName().toString().equalsIgnoreCase("HTC, Retest HIV status")) {
					secondHIVPosRetestObs = new Obs();
					secondHIVPosRetestObs = obs;
				}
			}

			if(rootConceptName.equalsIgnoreCase("HIV Treatment and Care Intake Template")) {
				hivIntakeForm = true;
				if (obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, ART start date")) {
					artStartDate = new Obs();
					artStartDate = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, ART Regimen")) {
					artStartRegimenObs = new Obs();
					artStartRegimenObs = obs;
				}
			}

			if(rootConceptName.equalsIgnoreCase("HTC, HIV Treatment and Care Intake - Counselor")) {
				hivIntakeCounselorForm = true;
				if(obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, Date transferred in")){
					transferInObs = new Obs();
					transferInObs = obs;
				}

				if(obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, Transferred in from")) {
					transferInSiteObs = new Obs();
					transferInSiteObs = obs;
				}
			}

			if (rootConceptName.equalsIgnoreCase("HIVTC, Viral Load Monitoring Form")) {
				viralLoadMonitoringForm = true;
				if (obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, Viral Load")) {
					hivViralLoadObs = new Obs();
					hivViralLoadObs = obs;
				}
			}

			if (rootConceptName.equalsIgnoreCase("HIV Treatment and Care Progress Template")) {
				hivFollowUpForm = true;
				if (obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, Viral Load")) {
					hivViralLoadObs = new Obs();
					hivViralLoadObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, CD4")) {
					baselineCD4Count = new Obs();
					baselineCD4Count = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, ART Regimen")) {
					currentARVRegimenObs = new Obs();
					currentARVRegimenObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("ART, Follow-up date")) {
					artFollowUpDateObs = new Obs();
					artFollowUpDateObs = obs;
				}

				if (obs.getConcept().getName().toString().equalsIgnoreCase("HIVTC, Date transferred out")) {
					transferOutObs = new Obs();
					transferOutObs = obs;
				}
			}
		}

		Section eddSection = null, flowsheetSection = null, vitalSignsSection = null, codedResultsSection = null, medicationsSection = null, probSection = null, allergySection = null;

		if(nutritionAssessmentForm || vitalsForm) {
			if (weightObs != null && heightObs != null && temperatureObs == null && systolicBpObs == null &&
					diastolicBpObs == null) {
				vitalSignsSection = vitalSignsSectionBuilder.generate(weightObs, heightObs);
			} else if(weightObs != null && heightObs != null && temperatureObs == null && systolicBpObs != null &&
					diastolicBpObs != null) {
				vitalSignsSection = vitalSignsSectionBuilder.generate(weightObs, heightObs, systolicBpObs, diastolicBpObs);
			} else if(weightObs != null && heightObs != null && temperatureObs != null && systolicBpObs != null &&
					diastolicBpObs != null) {
				vitalSignsSection = vitalSignsSectionBuilder.generate(weightObs, heightObs, temperatureObs,
						systolicBpObs, diastolicBpObs);
			}
		}

		if (htsForm && !htsRetestingForm) {
			if (firstHIVPosTestObs != null) {
				// Generate a Results Section for the HTS Test Result
				codedResultsSection = codedResultsSectionBuilder.generate(firstHIVPosTestObs);
			}
		}

		if (htsRetestingForm && !htsForm) {
			if (secondHIVPosRetestObs != null) {
				// Generate a Results Section for the HTS Retesting Result
				codedResultsSection = codedResultsSectionBuilder.generate(secondHIVPosRetestObs);
			}
		}

		if (htsForm && htsRetestingForm) {
			if (firstHIVPosTestObs != null && secondHIVPosRetestObs != null) {
				codedResultsSection = codedResultsSectionBuilder.generate(firstHIVPosTestObs, secondHIVPosRetestObs);
			}
		}

		if (hivIntakeForm || hivIntakeCounselorForm) {
			if (artStartDate != null && artStartRegimenObs != null)
				flowsheetSection = flowsheetSectionBuilder.generate(artStartDate, artStartRegimenObs);

			if (transferInObs != null) {
				flowsheetSection = flowsheetSectionBuilder.generate(transferInObs);
			}
		}

		if (hivFollowUpForm || viralLoadMonitoringForm) {
			if (hivViralLoadObs != null || currentARVRegimenObs != null || artFollowUpDateObs != null
					|| transferOutObs != null) {
				flowsheetSection = flowsheetSectionBuilder.generate(currentARVRegimenObs, artFollowUpDateObs,
						hivViralLoadObs, transferOutObs);
			}
		}

/*		if (estimatedDeliveryDateObs != null && lastMenstrualPeriodObs != null)
			eddSection = eddSectionBuilder.generate(estimatedDeliveryDateObs, lastMenstrualPeriodObs);

		if (gestgationalAgeObs != null && systolicBpObs != null && diastolicBpObs != null && weightObs != null)
			flowsheetSection = flowsheetSectionBuilder.generate(prepregnancyWeightObs, gestgationalAgeObs, fundalHeightObs,
			    presentationObs, systolicBpObs, diastolicBpObs, weightObs);

		if (systolicBpObs != null && diastolicBpObs != null && weightObs != null && heightObs != null
		        && temperatureObs != null)
			vitalSignsSection = vitalSignsSectionBuilder.generate(systolicBpObs, diastolicBpObs, weightObs, heightObs,
			    temperatureObs, baselineCD4Count, artStartRegimenObs, firstHIVPosTestObs, hivViralLoadObs, pregnancyStatusObs);

		if (systolicBpObs != null && diastolicBpObs != null && weightObs != null && heightObs != null
				&& temperatureObs != null) {
			vitalSignsSection = vitalSignsSectionBuilder.generate(systolicBpObs, diastolicBpObs, weightObs, heightObs,
					temperatureObs, baselineCD4Count, artStartRegimenObs, firstHIVPosTestObs, hivViralLoadObs,
					pregnancyStatusObs);
		}
*/

		Location visitLocation = Context.getLocationService().getDefaultLocation();

		if(encounter.getVisit() != null)
			visitLocation = encounter.getVisit().getLocation();

		// medicationsSection = medSectionBuilder.generate(medicationObs.toArray(new Obs[] {}));

		// Formatter
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ClinicalDocument doc = builder.generate(visitLocation, eddSection, codedResultsSection, flowsheetSection, vitalSignsSection, medicationsSection, probSection,
			    allergySection);

			XmlIts1Formatter formatter = EverestUtil.createFormatter();
			formatter.graph(baos, doc);

			return DocumentModel.createInstance(baos.toByteArray(), builder.getTypeCode(),
					XdsSenderConstants.CODE_SYSTEM_LOINC, builder.getFormatCode(), doc);
		} catch (Exception e) {
			log.error("Error generating document:", e);
			throw new RuntimeException(e);
		}
	}

	public String getRootConceptName(Obs obs) {
		if(obs.getObsGroup() == null) {
			return obs.getConcept().getName().toString();
		}
		return getRootConceptName(obs.getObsGroup());
	}
}
