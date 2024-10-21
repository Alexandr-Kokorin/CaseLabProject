package caselab.domain.entity.enums;

public enum DocumentPermissionName {
    READ, EDIT, SEND_FOR_SIGNING, SEND_FOR_VOTING, CREATOR;

    public boolean canRead() {
        return this == READ || this == EDIT || this == CREATOR;
    }

    public boolean canEdit() {
        return this == EDIT || this == CREATOR;
    }

    public boolean canSendForSigning() {
        return this == SEND_FOR_SIGNING || this == CREATOR;
    }

    public boolean canSendForVoting() {
        return this == SEND_FOR_VOTING || this == CREATOR;
    }
}
