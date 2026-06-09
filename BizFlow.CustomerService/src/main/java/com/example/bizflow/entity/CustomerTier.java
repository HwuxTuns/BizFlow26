package com.example.bizflow.entity;

/**
 * Enum định nghĩa bậc thành viên
 * - monthlyLimit: giới hạn điểm / tháng
 * - discountValue: số tiền giảm cho mỗi 100 điểm
 */
public enum CustomerTier {

    DONG(1000, 10000),
    BAC(3000, 12000),
    VANG(9000, 15000),
    BACH_KIM(15000, 22000),
    KIM_CUONG(25000, 30000);

    public final int monthlyLimit;
    public final int discountValue;

    CustomerTier(int monthlyLimit, int discountValue) {
        this.monthlyLimit = monthlyLimit;
        this.discountValue = discountValue;
    }

    public int discountForPoints(int points) {
        if (points < 100) {
            return 0;
        }
        int steps = points / 100;
        return steps * discountValue;
    }
}
