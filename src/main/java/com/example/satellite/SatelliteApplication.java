package com.example.satellite;

import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import com.example.satellite.service.FacilityScheduleSavingService;
import com.example.satellite.service.FacilityToAreaReferenceService;
import com.example.satellite.service.GreedyFacilityScheduleService;
import com.example.satellite.service.SatelliteMemoryObservanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

import java.io.File;
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

		//testing GreedyFacilityScheduleService
//		GreedyFacilityScheduleService scheduleService =
//				context.getBean(GreedyFacilityScheduleService.class);
//		List <SatelliteFacilitySession> facilitySchedule =
//				scheduleService.makeFacilitySchedule("Anadyr1");
//		System.out.println(facilitySchedule.size());
//		System.out.println(facilitySchedule.get(facilitySchedule.size()-1));

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
		FacilityToAreaReferenceService referenceService =
				context.getBean(FacilityToAreaReferenceService.class);
		referenceService.referFacilitySessionToAreaSession("Anadyr1")
				.forEach(System.out::println);

	}

}
