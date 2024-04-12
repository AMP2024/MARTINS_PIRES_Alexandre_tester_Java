package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

public class FareCalculatorServiceTest {

	private static final int ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000;
	private static final int THIRTY_MINUTES_IN_MSEC = 30 * 60 * 1000;
	private static final int FORTYFIVE_MINUTES_IN_MSEC = 45 * 60 * 1000;

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;
	private Date inTime;
	private Date outTime;

    @BeforeAll
	public static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	public void setUpPerTest() {
		ticket = new Ticket();
		// Set the out time as the current time
		outTime = new Date(System.currentTimeMillis());
	}

	private void setupTicketAndParkingSpot(ParkingType parkingType) {
		ParkingSpot parkingSpot = new ParkingSpot(1, parkingType, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
	}

	@Test
	public void calculateFareCar() {

		inTime = new Date(outTime.getTime() - (ONE_HOUR_IN_MILLISECONDS));

		setupTicketAndParkingSpot(ParkingType.CAR);

		fareCalculatorService.calculateFare(ticket);

		assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR, "Failed to calculate fare for car parking correctly");
	}

	@Test
	public void calculateFareBike() {

		inTime = new Date(outTime.getTime() - (ONE_HOUR_IN_MILLISECONDS));

		setupTicketAndParkingSpot(ParkingType.BIKE);

		fareCalculatorService.calculateFare(ticket);

		assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR, "Failed to calculate fare for bike parking correctly");
	}

	@Test
	public void calculateFareUnkownType() {

		inTime = new Date(outTime.getTime() - (ONE_HOUR_IN_MILLISECONDS));

		setupTicketAndParkingSpot(null);

		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket), "Expected NullPointerException was not thrown for unknown parking type");
	}

	@Test
	public void calculateFareBikeWithFutureInTime() {

		inTime = new Date(outTime.getTime() + (ONE_HOUR_IN_MILLISECONDS));

		setupTicketAndParkingSpot(ParkingType.BIKE);

		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket), "Expected IllegalArgumentException was not thrown for future in time for bike");
	}

	@Test
	public void calculateFareBikeWithLessThanOneHourParkingTime() {

		inTime = new Date(outTime.getTime() - (FORTYFIVE_MINUTES_IN_MSEC));

		setupTicketAndParkingSpot(ParkingType.BIKE);

		fareCalculatorService.calculateFare(ticket);

		double expectedPrice = 0.75 * Fare.BIKE_RATE_PER_HOUR;
		expectedPrice = Math.round(expectedPrice * 100.0) / 100.0;

		assertEquals(expectedPrice, ticket.getPrice(), 0.01, "Failed to calculate fare for bike parking less than an hour correctly");
	}

	@Test
	public void calculateFareCarWithLessThanOneHourParkingTime() {

		inTime = new Date(outTime.getTime() - (FORTYFIVE_MINUTES_IN_MSEC));

		setupTicketAndParkingSpot(ParkingType.CAR);

		fareCalculatorService.calculateFare(ticket);

		double expectedPrice = 0.75 * Fare.CAR_RATE_PER_HOUR;
		expectedPrice = Math.round(expectedPrice * 100.0) / 100.0;

		assertEquals(expectedPrice, ticket.getPrice(), 0.01, "Failed to calculate fare for car parking less than an hour correctly");
	}

	@Test
	public void calculateFareCarWithMoreThanADayParkingTime() {

		inTime = new Date(outTime.getTime() - (24 * ONE_HOUR_IN_MILLISECONDS)); // 24 hours parking time should give 24 *

		setupTicketAndParkingSpot(ParkingType.CAR);

		fareCalculatorService.calculateFare(ticket);

		assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice(), 0.01, "Failed to calculate fare for car parking more than a day correctly");
	}

	@Test
	public void calculateFareCarWithLessThan30minutesParkingTime() {

		inTime = new Date(outTime.getTime() - ((THIRTY_MINUTES_IN_MSEC) - 1)); // Less than 30 minutes ago - 1 Millisecond

		setupTicketAndParkingSpot(ParkingType.CAR);

		fareCalculatorService.calculateFare(ticket);

		assertEquals(0.0, ticket.getPrice(), 0.01, "Failed to calculate fare for car parking less than 30 minutes correctly");

	}

	@Test
	public void calculateFareBikeWithLessThan30minutesParkingTime() {

		inTime = new Date(outTime.getTime() - ((THIRTY_MINUTES_IN_MSEC) - 1)); // Less than 30 minutes ago - 1 Millisecond

		setupTicketAndParkingSpot(ParkingType.BIKE);

		fareCalculatorService.calculateFare(ticket);

		assertEquals(0.0, ticket.getPrice(), 0.01, "Failed to calculate fare for bike parking less than 30 minutes correctly");
	}

	@Test
	public void calculateFareCarWithDiscount() {

		inTime = new Date(outTime.getTime() - (FORTYFIVE_MINUTES_IN_MSEC)); // 45 minutes which is more than 30 minutes

		setupTicketAndParkingSpot(ParkingType.CAR);

		fareCalculatorService.calculateFare(ticket, true);

		double expectedPrice = 0.95 * Fare.CAR_RATE_PER_HOUR * 0.75; // 95% of the full rate for more than 30 minute
		expectedPrice = Math.round(expectedPrice * 100.0) / 100.0; // We round to two digits after the decimal point

		assertEquals(expectedPrice, ticket.getPrice(), 0.01, "Failed to calculate fare with discount for car correctly");
	}

	@Test
	public void calculateFareBikeWithDiscount() {

		inTime = new Date(outTime.getTime() - (FORTYFIVE_MINUTES_IN_MSEC)); // 45 minutes which is more than 30 minutes

		setupTicketAndParkingSpot(ParkingType.BIKE);

		fareCalculatorService.calculateFare(ticket, true);

		double expectedPrice = 0.95 * Fare.BIKE_RATE_PER_HOUR * 0.75; // 95% of the full rate for more than 30 minute
		expectedPrice = Math.round(expectedPrice * 100.0) / 100.0; // We round to two digits after the decimal point

		assertEquals(expectedPrice, ticket.getPrice(), 0.01, "Failed to calculate fare with discount for bike correctly");

	}

	// Test added
	@Test
	public void calculateFareBikeWithMoreThanADayParkingTime() {

		inTime = new Date(outTime.getTime() - (24 * ONE_HOUR_IN_MILLISECONDS)); // 24 hours parking time should give 24 *

		setupTicketAndParkingSpot(ParkingType.BIKE);

		fareCalculatorService.calculateFare(ticket);

		assertEquals((24 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice(), 0.01, "Failed to calculate fare for bike parking more than a day correctly");
	}

	// Test added
	@Test
	public void calculateFareCarWithFutureInTime() {

		inTime = new Date(outTime.getTime() + (ONE_HOUR_IN_MILLISECONDS));

		setupTicketAndParkingSpot(ParkingType.CAR);

		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket), "Expected IllegalArgumentException was not thrown for future in time for car");
	}

}
