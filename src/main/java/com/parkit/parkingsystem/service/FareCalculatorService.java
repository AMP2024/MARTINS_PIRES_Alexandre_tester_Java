package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private static final double MINIMUM_DURATION_FOR_CHARGE = 0.5; // 30 minutes = 0,5 heure

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();
        double duration = (double) (outTimeMillis - inTimeMillis) / (60 * 60 * 1000);

        // Vérifie si la durée est inférieure à 30 minutes
        if (duration < MINIMUM_DURATION_FOR_CHARGE) {
            ticket.setPrice(0.0);
        } else {
            // Applique la réduction de 5% si le paramètre discount est true
            double fareRate = (discount) ? 0.95 : 1.0;

            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * fareRate);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * fareRate);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }
    }

    // Méthode sans le paramètre discount
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}
