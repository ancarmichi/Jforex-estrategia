package com.example.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a trading signal that can be sent to Expert Advisors.
 * This class is immutable to ensure thread safety and provides validated
 * signal creation for both OPEN and CLOSE operations.
 */
public final class Signal implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Enumeration of possible signal types
     */
    public enum SignalType {
        OPEN,   // Signal to open a new position
        CLOSE   // Signal to close an existing position
    }
    
    /**
     * Enumeration of operation types for OPEN signals
     */
    public enum OperationType {
        BUY,
        SELL
    }
    
    private final String uniqueId;
    private final SignalType signalType;
    private final OperationType operationType;
    private final double stopPips;
    private final String instrument;

    /**
     * Creates a CLOSE signal for a specific instrument.
     * This factory method ensures proper initialization of CLOSE signals
     * without requiring operation type or stop pips parameters.
     * 
     * @param instrument The trading instrument
     * @return A new CLOSE signal
     * @throws IllegalArgumentException if instrument is null or empty
     */
    public static Signal createCloseSignal(String instrument) {
        return new Signal(SignalType.CLOSE, null, 0, instrument);
    }
    
    /**
     * Creates a new Signal instance.
     * For OPEN signals, requires valid operationType and stopPips.
     * For CLOSE signals, operationType must be null and stopPips must be 0.
     * 
     * @param signalType The type of signal (OPEN or CLOSE)
     * @param operationType The type of operation (BUY or SELL), only used for OPEN signals
     * @param stopPips The distance to stop loss in pips, only used for OPEN signals
     * @param instrument The trading instrument
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Signal(SignalType signalType, OperationType operationType, 
                 double stopPips, String instrument) {
        validateParameters(signalType, operationType, stopPips, instrument);
        
        this.uniqueId = generateUniqueId();
        this.signalType = signalType;
        this.operationType = operationType;
        this.stopPips = stopPips;
        this.instrument = instrument;
    }
    
    private String generateUniqueId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
        /**
     * Gets the unique identifier for this signal.
     * @return The unique identifier with "ORDER_" prefix
     */
    public String getUniqueId() { 
        return "ORDER_" + uniqueId; 
    }

    /**
     * Gets the type of signal (OPEN or CLOSE).
     * @return The signal type
     */
    public SignalType getSignalType() { 
        return signalType; 
    }

    /**
     * Gets the operation type (BUY or SELL). Only valid for OPEN signals.
     * @return The operation type, or null for CLOSE signals
     */
    public OperationType getOperationType() { 
        return operationType; 
    }

    /**
     * Gets the stop loss distance in pips. Only valid for OPEN signals.
     * @return The stop loss in pips, or 0 for CLOSE signals
     */
    public double getStopPips() { 
        return stopPips; 
    }

    /**
     * Gets the trading instrument name.
     * @return The instrument name
     */
    public String getInstrument() { 
        return instrument; 
    }
    
    /**
     * Converts this signal to its JSON representation.
     * Format:
     * For OPEN signals:
     * {
     *   "signalType": "OPEN",
     *   "operationType": "BUY",
     *   "uniqueId": "ORDER_12345",
     *   "stopPips": 20
     * }
     * For CLOSE signals:
     * {
     *   "signalType": "CLOSE",
     *   "uniqueId": "ORDER_12345"
     * }
     * @return A JSON string representing this signal
     */
    public String toJson() {
        StringBuilder json = new StringBuilder(128);
        json.append("{\n");
        json.append("  \"signalType\": \"").append(signalType).append("\"");
        
        if (signalType == SignalType.OPEN) {
            json.append(",\n  \"operationType\": \"").append(operationType).append("\"");
            json.append(",\n  \"stopPips\": ").append(String.format("%.1f", stopPips));
        }
        
        json.append(",\n  \"uniqueId\": \"ORDER_").append(uniqueId).append("\"");
        json.append("\n}");
        return json.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Signal)) return false;
        
        Signal other = (Signal) o;
        return uniqueId.equals(other.uniqueId) &&
               signalType == other.signalType &&
               Double.compare(other.stopPips, stopPips) == 0 &&
               instrument.equals(other.instrument) &&
               (operationType == other.operationType || 
                (operationType != null && operationType.equals(other.operationType)));
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + uniqueId.hashCode();
        result = 31 * result + signalType.hashCode();
        result = 31 * result + (operationType != null ? operationType.hashCode() : 0);
        result = 31 * result + Double.hashCode(stopPips);
        result = 31 * result + instrument.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Signal[ID: ORDER_").append(uniqueId);
        sb.append(", Type: ").append(signalType);
        if (signalType == SignalType.OPEN) {
            sb.append(", Operation: ").append(operationType);
            sb.append(", StopPips: ").append(String.format("%.1f", stopPips));
        }
        sb.append(", Instrument: ").append(instrument);
        sb.append("]");
        return sb.toString();
    }

    private void validateParameters(SignalType signalType, OperationType operationType, 
                                  double stopPips, String instrument) {
        if (signalType == null) {
            throw new IllegalArgumentException("Signal type cannot be null");
        }
        if (instrument == null || instrument.trim().isEmpty()) {
            throw new IllegalArgumentException("Instrument cannot be null or empty");
        }

        if (signalType == SignalType.OPEN) {
            if (operationType == null) {
                throw new IllegalArgumentException("Operation type cannot be null for OPEN signals");
            }
            if (stopPips <= 0) {
                throw new IllegalArgumentException("Stop pips must be positive for OPEN signals");
            }
        } else {
            // Validaciones específicas para señales CLOSE
            if (operationType != null) {
                throw new IllegalArgumentException("Operation type must be null for CLOSE signals");
            }
            if (stopPips != 0) {
                throw new IllegalArgumentException("Stop pips must be 0 for CLOSE signals");
            }
        }
    }
