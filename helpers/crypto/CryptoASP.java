package helpers.crypto;

public class CryptoASP {
	
	/*
VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
  Persistable = 0  'NotPersistable
  DataBindingBehavior = 0  'vbNone
  DataSourceBehavior  = 0  'vbNone
  MTSTransactionMode  = 0  'NotAnMTSObject
END
Attribute VB_Name = "cifra1"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
Private password

' Encipher the text using the pasword.
Private Function cipher(ByVal password As String, ByVal from_text As String) As String
Const MIN_ASC = 32  ' space
Const MAX_ASC = 126 ' ~
Const NUM_ASC = MAX_ASC - MIN_ASC + 1

Dim offset As Long
Dim str_len As Integer
Dim i As Integer
Dim ch As Integer

    ' Initialize the random number generator.
    offset = NumericPassword(password)
    Rnd -1
    Randomize offset

    ' Encipher the string.
    str_len = Len(from_text)
    For i = 1 To str_len
        ch = Asc(Mid$(from_text, i, 1))
        If ch >= MIN_ASC And ch <= MAX_ASC And ch Then
            ch = ch - MIN_ASC
            offset = Int((NUM_ASC + 1) * Rnd)
            ch = ((ch + offset) Mod NUM_ASC)
            ch = ch + MIN_ASC
            to_text = to_text & Chr$(ch)
        End If
    Next i
    cipher = to_text
End Function

' decipher the text using the pasword.
Private Function decipher(ByVal password As String, ByVal from_text As String) As String
Const MIN_ASC = 32  ' space
Const MAX_ASC = 126 ' ~
Const NUM_ASC = MAX_ASC - MIN_ASC + 1

Dim offset As Long
Dim str_len As Integer
Dim i As Integer
Dim ch As Integer

    ' Initialize the random number generator.
    offset = NumericPassword(password)
    Rnd -1
    Randomize offset

    ' Encipher the string.
    str_len = Len(from_text)
    For i = 1 To str_len
        ch = Asc(Mid$(from_text, i, 1))
        If ch >= MIN_ASC And ch <= MAX_ASC Then
            ch = ch - MIN_ASC
            offset = Int((NUM_ASC + 1) * Rnd)
            ch = ((ch - offset) Mod NUM_ASC)
            If ch < 0 Then ch = ch + NUM_ASC
            ch = ch + MIN_ASC
            to_text = to_text & Chr$(ch)
        End If
    Next i
    decipher = to_text
End Function

' Translate a password into an offset value.
Private Function NumericPassword(ByVal password As String) As Long
Dim value As Long
Dim ch As Long
Dim shift1 As Long
Dim shift2 As Long
Dim i As Integer
Dim str_len As Integer

    str_len = Len(password)
    For i = 1 To str_len
        ' Add the next letter.
        ch = Asc(Mid$(password, i, 1))
        value = value Xor (ch * 2 ^ shift1)
        value = value Xor (ch * 2 ^ shift2)

        ' Change the shift offsets.
        shift1 = (shift1 + 7) Mod 19
        shift2 = (shift2 + 13) Mod 23
    Next i
    NumericPassword = value
End Function

Public Function cifra(testo As Variant) As String 'il testo è di tipo Variant altrimenti in ASP non funziona.
    cifra = cipher(password, testo)
End Function

Public Function decifra(testo As Variant) As String
    decifra = decipher(password, testo)
End Function
Public Sub OnStartPage(ASPScriptingContext As ScriptingContext)
    password = "ajyfhry56dj38ht6"
End Sub
	 */
	
	private static final double[] RANDOM_VALUES = 
	{ -0.9405242
	, -0.01294231
	, -0.462619
	, -0.05029547
	, -0.6646464
	, -0.1269524
	, -0.5114579
	, -0.9811715
	, -0.8898407
	, -0.647903
	, -0.6090066
	, -0.9573241
	, -0.4049862
	, -0.0216558
	, -0.9801797
	, -0.6165098 };	

	private static final int MIN_ASC = 32;  // space
	private static final int MAX_ASC = 126; // ~
	private static final int NUM_ASC = MAX_ASC - MIN_ASC + 1;
	private static final double NUM_ASC_PIU_1 = NUM_ASC + 1; 
	
	private static char cifra(char ch, int pos) {
        int ich = (int)ch;
        if( ich >= MIN_ASC && ich <= MAX_ASC && ich != 0 ) {
        	ich -= MIN_ASC;
            ich -= (int)(NUM_ASC_PIU_1 * RANDOM_VALUES[pos]);
            ich %= NUM_ASC;
            ich += MIN_ASC;
        }
        char chCifrato = (char)ich;
        return chCifrato;
	}
	
	public static String esegui_cifratura(String testo) {
		StringBuffer out = new StringBuffer();
		for (int a = 0; a < testo.length(); a++) {
			out.append(cifra(testo.charAt(a), a));
		}
		return out.toString();
	}
}
