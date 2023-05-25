package com.example.satellite;

import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.SatelliteAreaSessionJdbcRepositoryImpl;
import com.example.satellite.service.FacilityScheduleSavingService;
import com.example.satellite.service.FacilityToAreaReferenceService;
import com.example.satellite.service.GreedyFacilityScheduleService;
import com.example.satellite.service.SatelliteMemoryObservanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan("com.example.satellite.config")
public class SatelliteApplication {

	private static ApplicationContext context;

	@Autowired
	public void context(ApplicationContext context){
		SatelliteApplication.context = context;
	}
	public static void main(String[] args) {
		SpringApplication.run(SatelliteApplication.class, args);


		FacilityToAreaReferenceService referenceService =
				context.getBean(FacilityToAreaReferenceService.class);
		SatelliteMemoryObservanceService memoryService =
				context.getBean(SatelliteMemoryObservanceService.class);
		FacilityScheduleSavingService savingService =
				context.getBean(FacilityScheduleSavingService.class);
		GreedyFacilityScheduleService scheduleService =
				context.getBean(GreedyFacilityScheduleService.class);
		SatelliteAreaSessionJdbcRepositoryImpl satelliteAreaSessionJdbcRepository =
				context.getBean(SatelliteAreaSessionJdbcRepositoryImpl.class);

		List <SatelliteFacilitySession> novosibSchedule = scheduleService.makeFacilitySchedule("Novosib");
		System.out.println("Schedule made. " + LocalDateTime.now());
		referenceService.referFacilitySessionToAreaSession(novosibSchedule);
		System.out.println("Referenced. " + LocalDateTime.now());
		novosibSchedule.forEach(sfs -> memoryService.makeShootingSessions(sfs.getSatellite()));
		System.out.println("Shooting sessions made." + LocalDateTime.now());

//		memoryService.makeTransferSessions(novosibSchedule);
//		System.out.println("Transferring sessions made.");
//		File scheduleFile = new File("/Users/juliavolkova/Desktop/test.txt");
//		savingService.saveSchedule(novosibSchedule, scheduleFile);


		//memoryService.makeTransferSessions(anadyr2Schedule);
//		File scheduleFile = new File("/Users/juliavolkova/Desktop/test.txt");
//		savingService.saveSchedule(anadyr2Schedule, scheduleFile);





		//testing FacilityScheduleSavingService
//		FacilityScheduleSavingService savingService =
//				context.getBean(FacilityScheduleSavingService.class);
//		File scheduleFile = new File("/Users/juliavolkova/Desktop/test2.txt");
//		savingService.saveSchedule("Magadan1", scheduleFile);

		//testing SatelliteMemoryObservanceService
//		SatelliteMemoryObservanceService observanceService =
//				context.getBean(SatelliteMemoryObservanceService.class);
//		List<SatelliteAreaSession> areaSessions =
//				observanceService.evaluateSessions("KinoSat_110701");
//		areaSessions.forEach(System.out::println);

		//testing FacilityToAreaReferenceService
//		FacilityToAreaReferenceService referenceService =
//				context.getBean(FacilityToAreaReferenceService.class);
//		referenceService.referFacilitySessionToAreaSession("Anadyr1")
//				.forEach(System.out::println);

	}

}
