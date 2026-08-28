package com.hubert.apartmentbooking.util;

public final class EmailTemplates {

    private EmailTemplates() {
    }

    public static String button(String title, String bodyHtml, String buttonText, String buttonUrl, String footerNote) {
        return """
                <!doctype html>
                <html lang="pl">
                <body style="margin:0; padding:0; background-color:#0f0f0f;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0f0f0f; padding:40px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:480px; background-color:#1a1a1a; border:1px solid #2e2e2e;">
                          <tr>
                            <td style="padding:40px; text-align:center;">
                              <p style="margin:0 0 24px; color:#b9905c; font-family:Arial,Helvetica,sans-serif; font-size:12px; letter-spacing:3px; text-transform:uppercase;">Residenza Aurea</p>
                              <h1 style="margin:0 0 16px; color:#f5f1ea; font-family:Georgia,'Times New Roman',serif; font-weight:400; font-size:26px;">%s</h1>
                              <div style="margin:0 0 32px; color:#b8b0a3; font-family:Arial,Helvetica,sans-serif; font-size:15px; line-height:1.6; text-align:left;">%s</div>
                              <table role="presentation" cellpadding="0" cellspacing="0" style="margin:0 auto;">
                                <tr>
                                  <td style="background-color:#b9905c; border-radius:2px;">
                                    <a href="%s" style="display:inline-block; padding:14px 32px; color:#0f0f0f; font-family:Arial,Helvetica,sans-serif; font-size:13px; font-weight:bold; letter-spacing:1px; text-transform:uppercase; text-decoration:none;">%s</a>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:20px 40px; border-top:1px solid #2e2e2e; text-align:center;">
                              <p style="margin:0; color:#7a7468; font-family:Arial,Helvetica,sans-serif; font-size:12px; line-height:1.5;">%s</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(title, bodyHtml, buttonUrl, buttonText, footerNote);
    }
}