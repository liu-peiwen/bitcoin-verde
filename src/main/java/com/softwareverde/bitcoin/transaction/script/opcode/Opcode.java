package com.softwareverde.bitcoin.transaction.script.opcode;

import com.softwareverde.bitcoin.util.ByteUtil;

public enum Opcode {
    // VALUE
    PUSH_NEGATIVE_ONE                   (0x4F),
    PUSH_ZERO                           (0x00),
    PUSH_VALUE                          (0x51, 0x60),
    PUSH_DATA                           (0x01, 0x4B),
    PUSH_DATA_BYTE                      (0x4C),
    PUSH_DATA_SHORT                     (0x4D),
    PUSH_DATA_INTEGER                   (0x4E),
    PUSH_VERSION                        (0x62, false),

    // DYNAMIC VALUE
    PUSH_STACK_SIZE                     (0x74),
    COPY_1ST                            (0x76),
    COPY_NTH                            (0x79),
    COPY_2ND                            (0x78),
    COPY_2ND_THEN_1ST                   (0x6E),
    COPY_3RD_THEN_2ND_THEN_1ST          (0x6F),
    COPY_4TH_THEN_3RD                   (0x70),
    COPY_1ST_THEN_MOVE_TO_3RD           (0x7D),

    // CONTROL
    IF                                  (0x63),
    NOT_IF                              (0x64),
    ELSE                                (0x67),
    END_IF                              (0x68),
    VERIFY                              (0x69),
    RETURN                              (0x6A),
    IF_VERSION                          (0x65, false),
    IF_NOT_VERSION                      (0x66, false),

    // STACK
    POP_TO_ALT_STACK                    (0x6B),
    POP_FROM_ALT_STACK                  (0x6C),
    IF_1ST_TRUE_THEN_COPY_1ST           (0x73),
    POP                                 (0x75),
    REMOVE_2ND_FROM_TOP                 (0x77),
    MOVE_NTH_TO_1ST                     (0x7A),
    ROTATE_TOP_3                        (0x7B),
    SWAP_1ST_WITH_2ND                   (0x7C),
    POP_THEN_POP                        (0x6D),
    MOVE_5TH_AND_6TH_TO_TOP             (0x71),
    SWAP_1ST_2ND_WITH_3RD_4TH           (0x72),

    // STRING
    STRING_CONCATENATE                  (0x7E, false),
    STRING_SUBSTRING                    (0x7F, false),
    STRING_LEFT                         (0x80, false),
    STRING_RIGHT                        (0x81, false),
    STRING_PUSH_LENGTH                  (0x82),

    // BITWISE
    BITWISE_INVERT                      (0x83, false),
    BITWISE_AND                         (0x84, false),
    BITWISE_OR                          (0x85, false),
    BITWISE_XOR                         (0x86, false),
    SHIFT_LEFT                          (0x98, false),
    SHIFT_RIGHT                         (0x99, false),

    // COMPARISON
    INTEGER_AND                         (0x9A),
    INTEGER_OR                          (0x9B),
    IS_EQUAL                            (0x87),
    IS_EQUAL_THEN_VERIFY                (0x88),
    IS_TRUE                             (0x92),
    IS_NUMERICALLY_EQUAL                (0x9C),
    IS_NUMERICALLY_EQUAL_THEN_VERIFY    (0x9D),
    IS_NUMERICALLY_NOT_EQUAL            (0x9E),
    IS_LESS_THAN                        (0x9F),
    IS_GREATER_THAN                     (0xA0),
    IS_LESS_THAN_OR_EQUAL               (0xA1),
    IS_GREATER_THAN_OR_EQUAL            (0xA2),
    IS_WITHIN_RANGE                     (0xA5),

    // ARITHMETIC
    ADD_ONE                             (0x8B),
    SUBTRACT_ONE                        (0x8C),
    MULTIPLY_BY_TWO                     (0x8D, false),
    DIVIDE_BY_TWO                       (0x8E, false),
    NEGATE                              (0x8F),
    ABSOLUTE_VALUE                      (0x90),
    NOT                                 (0x91),
    ADD                                 (0x93),
    SUBTRACT                            (0x94),
    MULTIPLY                            (0x95, false),
    DIVIDE                              (0x96, false),
    MODULUS                             (0x97, false),
    MIN                                 (0xA3),
    MAX                                 (0xA4),

    // CRYPTOGRAPHIC
    RIPEMD_160                          (0xA6),
    SHA_1                               (0xA7),
    SHA_256                             (0xA8),
    SHA_256_THEN_RIPEMD_160             (0xA9),
    DOUBLE_SHA_256                      (0xAA),
    CHECK_SIGNATURE                     (0xAC),
    CHECK_SIGNATURE_THEN_VERIFY         (0xAD),
    CHECK_MULTISIGNATURE                (0xAE),
    CHECK_MULTISIGNATURE_THEN_VERIFY    (0xAF),

    CODE_SEPARATOR                      (0xAB), //  CODE_SEPARATOR's intended use was to designate where signed-content is supposed to begin (rendering parts of the script mutable).
                                                //  Its benefit seems rare and borderline useless, and is likely a security risk.
                                                //  https://bitcoin.stackexchange.com/questions/34013/what-is-op-codeseparator-used-for

    // LOCK TIME
    CHECK_LOCK_TIME_THEN_VERIFY         (0xB1),
    CHECK_SEQUENCE_NUMBER_THEN_VERIFY   (0xB2),

    // NO OPERATION
    NO_OPERATION                        (0x61),
    NO_OPERATION_1                      (0xB0),
    NO_OPERATION_2                      (0xB3, 0xB9),
    RESERVED                            (0x50, false),
    RESERVED_1                          (0x89, 0x8A, false)

    ; // END ENUMS

    private final boolean _isEnabled;
    private final int _minValue;
    private final int _maxValue;

    Opcode(final int base) {
        _minValue = base;
        _maxValue = base;
        _isEnabled = true;
    }

    Opcode(final int base, final boolean isEnabled) {
        _minValue = base;
        _maxValue = base;
        _isEnabled = isEnabled;
    }

    Opcode(final int minValue, final int maxValue) {
        _minValue = minValue;
        _maxValue = maxValue;
        _isEnabled = true;
    }

    Opcode(final int minValue, final int maxValue, final boolean isEnabled) {
        _minValue = minValue;
        _maxValue = maxValue;
        _isEnabled = isEnabled;
    }

    public boolean isEnabled() { return _isEnabled; }
    public byte getValue() { return (byte) _minValue; }
    public int getMinValue() { return _minValue; }
    public int getMaxValue() { return _maxValue; }

    public boolean matchesByte(byte b) {
        final int bValue = ByteUtil.byteToInteger(b);
        return (_minValue <= bValue && bValue <= _maxValue);
    }
}
