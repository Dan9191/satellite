package com.example.satellite;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		FacilityScheduleSavingService savingService =
				context.getBean(FacilityScheduleSavingService.class);
		GreedyFacilityScheduleService scheduleService =
				context.getBean(GreedyFacilityScheduleService.class);
		MemoryObservanceService memoryObservanceService =
				context.getBean(MemoryObservanceService.class);

		System.out.println("Application started at " + LocalDateTime.now());

		List <SatelliteFacilitySession> schedule = scheduleService.makeFacilitySchedule("Anadyr1");
		System.out.println("Schedule made. " + LocalDateTime.now());

		referenceService.referFacilitySessionToAreaSession(schedule);
		System.out.println("Referenced. " + LocalDateTime.now());

		Facility facility = schedule.get(0).getFacility();

		Set<Satellite> satellites = schedule.stream()
				.map(SatelliteFacilitySession::getSatellite)
						.collect(Collectors.toSet());
		satellites.forEach(memoryObservanceService::makeShootingSessions);
		System.out.println("Shooting sessions made. " + LocalDateTime.now());

		schedule.forEach(sfs -> memoryObservanceService.makeTransferringSessions(facility, sfs.getSatellite()));
		System.out.println("Transferring sessions made. " + LocalDateTime.now());

		File scheduleFile = new File("/Users/juliavolkova/Desktop/test.txt");
		savingService.saveSchedule(schedule, scheduleFile);
		System.out.println("Schedule saved. " + LocalDateTime.now());

	}

}
