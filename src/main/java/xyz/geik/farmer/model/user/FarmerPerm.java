package xyz.geik.farmer.model.user;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Enum class of farmer permission of user
 */
public enum FarmerPerm {
    COOP,
    MEMBER,
    OWNER;

    /**
     * Gets role of farmer by int id
     *
     * @param id
     * @return
     */
    public static FarmerPerm getRole(int id) {
        switch (id) {
            case 1:
                return FarmerPerm.MEMBER;
            case 2:
                return FarmerPerm.OWNER;
            default:
                return FarmerPerm.COOP;
        }
    }

    /**
     * Gets int id of role
     * @param perm
     * @return
     */
    @Contract(pure = true)
    public static int getRoleId(@NotNull FarmerPerm perm) {
        switch (perm) {
            case MEMBER:
                return 1;
            case OWNER:
                return 2;
            default:
                return 0;
        }
    }
}