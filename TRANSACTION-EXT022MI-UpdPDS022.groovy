/**
 * README
 * This extension is being triggered by EXT022MI/UpdPDS022
 *
 * Name: UpdPDS022
 * Description: XTend made to update an enreg in MPDSST 
 * Date       Changed By                     Description
 * 20231013   Ludovic TRAVERS                Create EXT022MI_UpdPDS022
 */
 
public class UpdPDS022 extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  
  public UpdPDS022(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.utility = utility
  }

  int cono
  int chno
  String chid
  String faci
  String plgr
  
  char cktp
  String cmky
  String csty
  char pktp
  String pmky
  String psty
  
  int fdat
  int tdat
  String fdatS
  String tdatS
  
  double seti
  String mxid
  int cmac
  int pmac
  char sett
  
  public void main() {
    cono = (Integer) program.getLDAZD().CONO
    if (!mi.inData.get("CONO").trim().isEmpty()) {
      cono = utility.call("NumberUtil","parseStringToInteger", mi.inData.get("CONO"))
    }
    chid = program.getUser()
    
    faci = mi.inData.get("FACI").trim()
    plgr = mi.inData.get("PLGR").trim()
    cktp = mi.inData.get("CKTP").charAt(0)
    cmky = mi.inData.get("CMKY").trim()
    csty = mi.inData.get("CSTY").trim()
    pktp = mi.inData.get("PKTP").charAt(0)
    pmky = mi.inData.get("PMKY").trim()
    psty = mi.inData.get("PSTY").trim()
    
    DBAction query = database.table("MPDSST")
      .index("00")
      .build()
    
    DBContainer container = query.getContainer()
    
    container.set("PTCONO", cono)
    
    container.set("PTFACI", mi.inData.get("FACI").trim())
    container.set("PTPLGR", mi.inData.get("PLGR").trim())
  
    if (!mi.inData.get("CKTP").trim().isEmpty()) {
      container.set("PTCKTP", mi.inData.get("CKTP").charAt(0))
    }
    else {
      container.set("PTCKTP", ' ')
    }
    
    container.set("PTCMKY", mi.inData.get("CMKY").trim())
    
    container.set("PTCSTY", mi.inData.get("CSTY").trim())
    
    if (!mi.inData.get("PKTP").trim().isEmpty()) {
      container.set("PTPKTP", mi.inData.get("PKTP").charAt(0))
    }
    else {
      container.set("PTPKTP", ' ')
    }
    
    container.set("PTPMKY", mi.inData.get("PMKY").trim())
    container.set("PTPSTY", mi.inData.get("PSTY").trim())
   
    fdat = utility.call("NumberUtil","parseStringToInteger", mi.inData.get("FDAT"))
    fdatS = mi.inData.get("FDAT").trim()
   
    container.set("PTFDAT", fdat)
    
    
    Closure<?> updateCallBack = { LockedResult lockedResult ->
    
      tdat = utility.call("NumberUtil","parseStringToInteger", mi.inData.get("TDAT"))
      tdatS = mi.inData.get("TDAT").trim()
  
      if (!tdatS.isEmpty()) {
        if (!utility.call("DateUtil","isDateValid", tdatS, "yyyyMMdd")) {
           mi.error("Date de fin $tdatS n'est pas valide")
           return
        }
        
        if (tdat < fdat) {
           mi.error("Date de fin $tdatS ne peut pas être inférieure à date de début $fdatS ")
           return
        }
      }

      lockedResult.set("PTTDAT", tdat)
   
      seti = utility.call("NumberUtil","parseStringToDouble", mi.inData.get("SETI"))
      mxid = mi.inData.get("MXID").trim()
      if (seti < 0) {
        mi.error("Setup time $seti is invalid")
        return
      }
      
      if ((seti > 0) && !mxid.isEmpty()) {
        mi.error("Only one of setup time and matrix_ID should be entered")
        return
      }

      cmac = utility.call("NumberUtil","parseStringToInteger", mi.inData.get("CMAC"))
      pmac = utility.call("NumberUtil","parseStringToInteger", mi.inData.get("PMAC"))
      if (!mxid.isEmpty()) {
        if (!checkMPMXID(mxid)) {
          return
        }
      }
      
      sett = mi.inData.get("SETT").charAt(0)
      if ((sett != '1') && (sett != '2')) {
        mi.error("Setup time type $sett is invalid")
        return
      }
      
      lockedResult.set("PTSETI", utility.call("NumberUtil","parseStringToDouble", mi.inData.get("SETI")))
      lockedResult.set("PTMXID", mi.inData.get("MXID").trim())                          
      lockedResult.set("PTCMAC", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("CMAC")))
      lockedResult.set("PTPMAC", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("PMAC")))
      if (!mi.inData.get("SETT").trim().isEmpty()) {
        lockedResult.set("PTSETT", mi.inData.get("SETT").charAt(0))
      }
      else {
        lockedResult.set("PTSETT", ' ')
      }
     
      chno = lockedResult.get("PTCHNO")
      chno++
      lockedResult.set("PTLMDT", utility.call("DateUtil","currentDateY8AsInt"))
      lockedResult.set("PTCHNO", chno)
      lockedResult.set("PTCHID", chid)
      
      lockedResult.update()
    }
    
    if (!query.readLock(container, updateCallBack)) {
      mi.error("L'enregistrement n'existe pas.")
    }
  }
  
  /**
  * On vérifie si le produit passé en paramètre d'entrée existe dans MPMXID
  */
  private boolean checkMPMXID(String MXID) {
    int MXTP = 0
    int XI = cmac + pmac
    
    DBAction query = database.table("MPMXID")
      .index("00")
      .selection("QAMXTP")
      .build()
    DBContainer container = query.getContainer()
    container.set("QACONO", cono)
    container.set("QAMXID", MXID)
    if (!query.read(container)) {
      mi.error("Scale $MXID does not exist")
      return false
    }
    else {
      MXTP = container.get("QAMXTP")
      if (MXTP != 1) {
        mi.error("Selection matrix type &MXID is invalid")
        return false
      }
      if (XI == 0) {
        mi.error("Column &cmac does not exist")
        return false
      }
      if (XI > 9) {
        mi.error("Column &XI is invalid")
        return false
      }
    }
    return true
  } 
}