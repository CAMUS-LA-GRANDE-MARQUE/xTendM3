/**
 * README
 * Mise à jour du flag FSLEDG.ESIICD
 *
 * Name: ChangeIICD
 * Description: 
 * Date       Changed By                     Description
 * 20231018   François Leprévost             Création verbe ChangeIICD sur la table FSLEDG
 */
public class ChangeIICD extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  
  public ChangeIICD(MIAPI mi, DatabaseAPI database , ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.program = program
    this.utility = utility
  }
  
  int cono = 0
  String chid = ""
  
  public void main() {
    cono = (Integer) program.getLDAZD().CONO
    chid = program.getUser()
    String divi = mi.inData.get("DIVI").trim()
    String yea4 = mi.inData.get("YEA4").trim()
    String jrno = mi.inData.get("JRNO").trim()
    String jsno = mi.inData.get("JSNO").trim()
    String iicd = mi.inData.get("IICD")
    
    if (divi.isEmpty()) {
      mi.error("La division est obligatoire.")
      return
    } else if (!checkDiviExist(divi)) {
      mi.error("La division est inexistante.")
      return
    }
    
    if (yea4.isEmpty()) {
      mi.error("L'année est obligatoire.")
      return
    } else if (!utility.call("NumberUtil","isValidNumber", yea4, ".")) {
      mi.error("L'année est incorrecte.")
      return
    }
    
    if (jrno.isEmpty()) {
      mi.error("Le numéro de journal est obligatoire.")
      return
    } else if (!utility.call("NumberUtil","isValidNumber", jrno, ".")) {
      mi.error("Le numéro de journal est incorrect.")
      return
    }
    
    if (jsno.isEmpty()) {
      mi.error("Le numéro de séquence journal est obligatoire.")
      return
    } else if (!utility.call("NumberUtil","isValidNumber", jsno, ".")) {
      mi.error("Le numéro de séquence journal est incorrect.")
      return
    }
    
    if (iicd.isEmpty()) {
      mi.error("Le champ IICD est obligatoire.")
      return
    } else {
      updateFSLEDG(divi, yea4, jrno, jsno, iicd)
    }
  }
  
  /**
   * On vérifie que la DIVI existe
  */
  private boolean checkDiviExist(String divi) {
    DBAction query = database.table("CMNDIV").index("00").build()
    DBContainer container = query.getContainer()
    container.set("CCCONO", cono)
    container.set("CCDIVI", divi)
    
    return query.read(container)
  }
  
  /**
  * Mise à jour de l'enregistrement
  */
  private void updateFSLEDG(String divi, String yea4, String jrno, String jsno, String iicd) {
    DBAction query = database.table("FSLEDG").index("00").build()
    DBContainer container = query.getContainer()

    container.set("ESCONO", cono)
    container.set("ESDIVI", divi)
    container.set("ESYEA4", utility.call("NumberUtil","parseStringToInteger", yea4))
    container.set("ESJRNO", utility.call("NumberUtil","parseStringToInteger", jrno))
    container.set("ESJSNO", utility.call("NumberUtil","parseStringToInteger", jsno))
    
    Closure<?> updateCallBack = { LockedResult lockedResult ->
      int chno = lockedResult.get("ESCHNO")
      chno++
      lockedResult.set("ESLMDT", utility.call("DateUtil","currentDateY8AsInt"))
      lockedResult.set("ESCHNO", chno)
      lockedResult.set("ESCHID", chid)
      lockedResult.set("ESIICD", utility.call("NumberUtil","parseStringToInteger", iicd))
      
      lockedResult.update()
    }
    
    if (!query.readLock(container, updateCallBack)) {
      mi.error("L'enregistrement n'existe pas.")
    } 
  }
}
  
