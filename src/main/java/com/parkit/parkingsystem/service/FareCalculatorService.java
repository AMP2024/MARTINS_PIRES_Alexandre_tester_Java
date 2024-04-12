package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import static com.parkit.parkingsystem.constants.Fare.MINIMUM_DURATION_FOR_CHARGE;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();
        double duration = (double) (outTimeMillis - inTimeMillis) / (60 * 60 * 1000);

        // Check if the duration is less than 30 minutes"
        if (duration < MINIMUM_DURATION_FOR_CHARGE) {
            ticket.setPrice(0.0);
        } else {
            // Apply the 5% discount if the discount parameter is true
            double fareRate = (discount) ? 0.95 : 1.0;
            double price;

            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    price = duration * Fare.CAR_RATE_PER_HOUR * fareRate;
                    price = Math.round(price * 100.0) / 100.0;
                    ticket.setPrice(price);
                    break;
                }
                case BIKE: {
                    price = duration * Fare.BIKE_RATE_PER_HOUR * fareRate;
                    price = Math.round(price * 100.0) / 100.0;
                    ticket.setPrice(price);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }
    }

    // Method without the discount parameter
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}
