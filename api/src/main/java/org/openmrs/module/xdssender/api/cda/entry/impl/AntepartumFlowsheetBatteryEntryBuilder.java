package org.openmrs.module.xdssender.api.cda.entry.impl;

import org.apache.commons.lang.NotImplementedException;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActRelationshipHasComponent;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActClassDocumentEntryOrganizer;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.xdssender.XdsSenderConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Builds the antepartum flowsheet battery entry
 * 
 * @author JustinFyfe
 */
@Component("xdssender.AntepartumFlowsheetBatteryEntryBuilder")
public class AntepartumFlowsheetBatteryEntryBuilder extends EntryBuilderImpl {

	@Autowired
	private SimpleObservationEntryBuilder obsBuilder;
	
	/**
	 * Generate the flowsheet battery
	 */
	public Organizer generate(Obs gestgationalAgeObs, Obs fundalHeightObs, Obs presentationObs, Obs systolicBpObs,
	        Obs diastolicBpObs, Obs weightObs) {
		Encounter batteryEnc = gestgationalAgeObs.getEncounter();
		
		if (fundalHeightObs != null && !batteryEnc.getId().equals(fundalHeightObs.getEncounter().getId())
		        || presentationObs != null && !batteryEnc.getId().equals(presentationObs.getEncounter().getId())
		        || systolicBpObs != null && !batteryEnc.getId().equals(systolicBpObs.getEncounter().getId())
		        || diastolicBpObs != null && !batteryEnc.getId().equals(diastolicBpObs.getEncounter().getId())
		        || weightObs != null && !batteryEnc.getId().equals(weightObs.getEncounter().getId()))
			throw new IllegalArgumentException("All arguments for the flowsheet panel must come from the same encounter");
		
		Organizer batteryOrganizer = super.createOrganizer(x_ActClassDocumentEntryOrganizer.BATTERY, Collections
		        .singletonList(XdsSenderConstants.ENT_TEMPLATE_ANTEPARTUM_FLOWSHEET_PANEL), new CD<String>("57061-4",
		        XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
		        "Antepartum Flowsheet Panel", null), new II(getConfiguration().getEncounterRoot(), batteryEnc.getId()
		        .toString()), ActStatus.Completed, batteryEnc.getEncounterDatetime());
		
		// SimpleObservationEntryBuilder obsBuilder = new SimpleObservationEntryBuilder();

		batteryOrganizer.getComponent().add(
		    new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>("11884-4",
		            XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
		            "Gestational Age", null), gestgationalAgeObs)));
		
		if (fundalHeightObs != null)
			batteryOrganizer.getComponent().add(
			    new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
			            "11881-0", XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
			            "Fundal Height by tapemeasure", null), fundalHeightObs)));
		if (systolicBpObs != null)
			batteryOrganizer.getComponent().add(
			    new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
			            "8480-6", XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
			            "Blood pressure - Systolic", null), systolicBpObs)));
		if (diastolicBpObs != null)
			batteryOrganizer.getComponent().add(
			    new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
			            "8462-4", XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
			            "Blood pressure - Diastolic", null), diastolicBpObs)));
		if (weightObs != null)
			batteryOrganizer.getComponent().add(
			    new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
			            "3141-9", XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
			            "Body weight measured", null), weightObs)));
		
		return batteryOrganizer;
	}

	/**
	 * Generate the flowsheet battery
	 */
	public Organizer generate(Obs artStartDate, Obs artStartRegimen) {
		Encounter batteryEnc = null;

		if(artStartDate != null) {
			batteryEnc = artStartDate.getEncounter();
		}

		if (artStartRegimen != null && !batteryEnc.getId().equals(artStartRegimen.getEncounter().getId())) {
			throw new IllegalArgumentException("All arguments for the flowsheet panel must come from the same encounter");
		}

		Organizer batteryOrganizer = super.createOrganizer(x_ActClassDocumentEntryOrganizer.BATTERY, Collections
				.singletonList(XdsSenderConstants.ENT_TEMPLATE_ANTEPARTUM_FLOWSHEET_PANEL), new CD<String>("57061-4",
				XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
				"Antepartum Flowsheet Panel", null), new II(getConfiguration().getEncounterRoot(), batteryEnc.getId()
				.toString()), ActStatus.Completed, batteryEnc.getEncounterDatetime());

		if (artStartRegimen != null) {
			batteryOrganizer.getComponent().add(
					new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
							"1088", XdsSenderConstants.CODE_SYSTEM_CIEL, XdsSenderConstants.CODE_SYSTEM_NAME_SNOMED, null,
							"HIV treatment regimen", null), artStartRegimen)));
		}

		if (artStartDate != null) {
			batteryOrganizer.getComponent().add(
					new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
							"159599", XdsSenderConstants.CODE_SYSTEM_CIEL, XdsSenderConstants.CODE_SYSTEM_NAME_CIEL, null,
							"Antiretroviral treatment start date", null), artStartDate)));
		}

		return batteryOrganizer;
	}

	/**
	 * Generate the flowsheet battery
	 */
	public Organizer generate(Obs currentARVRegimenObs, Obs artFollowUpDateObs, Obs hivViralLoadObs, Obs transferOutObs) {
		Encounter batteryEnc = null;

		if(currentARVRegimenObs != null) {
			batteryEnc = currentARVRegimenObs.getEncounter();
		}

		if (artFollowUpDateObs != null && !batteryEnc.getId().equals(artFollowUpDateObs.getEncounter().getId())
				|| hivViralLoadObs != null && !batteryEnc.getId().equals(hivViralLoadObs.getEncounter().getId())
				|| transferOutObs != null && !batteryEnc.getId().equals(transferOutObs.getEncounter().getId())) {
			throw new IllegalArgumentException("All arguments for the flowsheet panel must come from the same encounter");
		}

		Organizer batteryOrganizer = super.createOrganizer(x_ActClassDocumentEntryOrganizer.BATTERY, Collections
				.singletonList(XdsSenderConstants.ENT_TEMPLATE_ANTEPARTUM_FLOWSHEET_PANEL), new CD<String>("57061-4",
				XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
				"Antepartum Flowsheet Panel", null), new II(getConfiguration().getEncounterRoot(), batteryEnc.getId()
				.toString()), ActStatus.Completed, batteryEnc.getEncounterDatetime());

		if (currentARVRegimenObs != null) {
			batteryOrganizer.getComponent().add(
					new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
							"162240", XdsSenderConstants.CODE_SYSTEM_CIEL, XdsSenderConstants.CODE_SYSTEM_NAME_CIEL, null,
							"HIV treatment regimen", null), currentARVRegimenObs)));
		}

		if (artFollowUpDateObs != null) {
			batteryOrganizer.getComponent().add(
					new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
							"5096", XdsSenderConstants.CODE_SYSTEM_CIEL, XdsSenderConstants.CODE_SYSTEM_NAME_CIEL, null,
							"RETURN VISIT DATE", null), artFollowUpDateObs)));
		}

		if (hivViralLoadObs != null) {
			batteryOrganizer.getComponent().add(
					new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
							"856", XdsSenderConstants.CODE_SYSTEM_CIEL, XdsSenderConstants.CODE_SYSTEM_NAME_CIEL, null,
							"HIV VIRAL LOAD", null), hivViralLoadObs)));
		}

		if (transferOutObs != null) {
			batteryOrganizer.getComponent().add(
					new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
							"160649", XdsSenderConstants.CODE_SYSTEM_CIEL, XdsSenderConstants.CODE_SYSTEM_NAME_CIEL, null,
							"Date transferred out", null), transferOutObs)));
		}

		return batteryOrganizer;
	}

	/**
	 * Generate the flowsheet battery
	 */
	public Organizer generate(Obs transferInObs) {
		Encounter batteryEnc = null;

		if(transferInObs != null) {
			batteryEnc = transferInObs.getEncounter();
		}

		Organizer batteryOrganizer = super.createOrganizer(x_ActClassDocumentEntryOrganizer.BATTERY, Collections
				.singletonList(XdsSenderConstants.ENT_TEMPLATE_ANTEPARTUM_FLOWSHEET_PANEL), new CD<String>("57061-4",
				XdsSenderConstants.CODE_SYSTEM_LOINC, XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null,
				"Antepartum Flowsheet Panel", null), new II(getConfiguration().getEncounterRoot(), batteryEnc.getId()
				.toString()), ActStatus.Completed, batteryEnc.getEncounterDatetime());

		if (transferInObs != null) {
			batteryOrganizer.getComponent().add(
					new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, obsBuilder.generate(new CD<String>(
							"160534", XdsSenderConstants.CODE_SYSTEM_CIEL, XdsSenderConstants.CODE_SYSTEM_NAME_CIEL, null,
							"Transfer in date", null), transferInObs)));
		}

		return batteryOrganizer;
	}

	/**
	 * Generate the flowsheet batter based on a grouping obs (TBD)
	 */
	public ClinicalStatement generate(BaseOpenmrsData data) {
		throw new NotImplementedException();
	}
	
}
