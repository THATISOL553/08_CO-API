package com.coapi.service;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coapi.binding.CoResponse;
import com.coapi.entity.CitizenApiEntity;
import com.coapi.entity.CoTriggerEntity;
import com.coapi.entity.DcCasesEntity;
import com.coapi.entity.EligDtlsEntity;
import com.coapi.repository.CitizenAppRepository;
import com.coapi.repository.CoTriggerRepository;
import com.coapi.repository.DcCasesRepository;
import com.coapi.repository.EligDtlsRepository;
import com.coapi.utils.EmailUtils;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class CoServiceImpl implements CoService {

	@Autowired
	private CoTriggerRepository coTrgRepo;
	
	@Autowired
	private EligDtlsRepository eligRepo;
	
	@Autowired
	private CitizenAppRepository appRepo;
	
	@Autowired
	private DcCasesRepository dcCaseRepo;
	
	@Autowired
	private EmailUtils emailUtils;
	
	@Override
	public CoResponse processPendingTriggers() {
		
		CoResponse response = new CoResponse();
		
		
		CitizenApiEntity appEntity = null;
		//fetch all pending triggers
		List<CoTriggerEntity> pendingTriggers = coTrgRepo.findByTrgStatus("Pending");
		
		response.setTotalTriggers(Long.valueOf(pendingTriggers.size()));
		
		//process each Pending trigger
		for(CoTriggerEntity entity : pendingTriggers) {
			
			//get eligibility data based on caseNum
			EligDtlsEntity eligRecord = eligRepo.findByCaseNum(entity.getCaseNum());

			//get citizen data based on caseNum
			Optional<DcCasesEntity> findById = dcCaseRepo.findById(entity.getCaseNum());
			if (findById.isPresent()) {
				DcCasesEntity dcCasesEntity = findById.get();
				Integer appId = dcCasesEntity.getAppId();
				Optional<CitizenApiEntity> appEntityOptional = appRepo.findById(appId);
				
				if (appEntityOptional.isPresent()) {
					appEntity = appEntityOptional.get();
				}
			}
			
			//generate pdf with elig details & send the pdf to citize mail
			
			Long success = 0l;
			Long failed = 0l;
			
			try {
				generateAndSendPdf(eligRecord, appEntity);
				success++;
			} catch (Exception e) {
				e.printStackTrace();
				failed++;
			}
			response.setSuccTriggers(success);
			response.setFailedTriggers(failed);
			
		}
		
		//return summary
		return response;
	}
	
	private void generateAndSendPdf(EligDtlsEntity eligData, CitizenApiEntity appEntity) throws Exception {
		Document document = new Document(PageSize.A4);
		FileOutputStream fos = null;
		File file = new File(eligData.getCaseNum()+".pdf");
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		PdfWriter.getInstance(document, fos);
		
		document.open();
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(18);
		font.setColor(Color.BLUE);
		
		Paragraph p = new Paragraph("ELIGIBILITY REPORT", font);
		p.setAlignment(Paragraph.ALIGN_CENTER);
		
		document.add(p);
		
		PdfPTable table = new PdfPTable(7);
		table.setWidthPercentage(100f);
		table.setWidths(new float[] {1.5f, 3.5f, 3.0f, 1.5f, 3.0f, 1.5f, 3.0f});
		table.setSpacingBefore(10);
		
		PdfPCell cell = new PdfPCell();
		
		cell.setBackgroundColor(Color.BLUE);
		cell.setPadding(5);
		
		font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setColor(Color.WHITE);
		
		cell.setPhrase(new Phrase("Citizen Name", font));
		table.addCell(cell);
			
		cell.setPhrase(new Phrase("Plan Name", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Plan Status", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Plan Start Date", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Plan End Date", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Benefot Amount", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Denial Reason", font));
		table.addCell(cell);
		
			table.addCell(appEntity.getFullName());
			table.addCell(eligData.getPlanName());
			table.addCell(eligData.getPlanStatus());
			table.addCell(eligData.getPlanStartDate()+"");
			table.addCell(eligData.getPlanEndDate()+"");
			table.addCell(eligData.getBenefitAmt()+"");
			table.addCell(eligData.getDenialReason()+"");
		document.add(table);
		document.close();
		
		
		String subject = "HIS Eligibility Info";
		String body = "HIS Eligibility Info";
		emailUtils.sendEmail(subject, body,appEntity.getEmail() , file);
		updateTrigger(eligData.getCaseNum(), file);
		
		//file.delete();
	}
	
	private void updateTrigger(Long caseNum, File file) throws Exception {
		CoTriggerEntity coTriggerEntity = coTrgRepo.findByCaseNum(caseNum);
		
		byte[] arr = new byte[(byte) file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(arr);
		
		coTriggerEntity.setCoPdf(arr);
		coTriggerEntity.setTrgStatus("Completed");
		coTrgRepo.save(coTriggerEntity);
		
		fis.close();
	}
}
