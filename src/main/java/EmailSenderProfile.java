public class EmailSenderProfile {

    private String senderName;
    private String senderAddress;
    private String profileImageUrl = "https://ssl.gstatic.com/ui/v1/icons/mail/profile_mask2.png";

    public EmailSenderProfile(String senderName, String senderAddress, String profileImageUrl) {
        this.senderName = senderName;
        this.senderAddress = senderAddress.toLowerCase();
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress.toLowerCase();
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
