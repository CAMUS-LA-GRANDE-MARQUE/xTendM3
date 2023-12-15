/**
 * README
 * This extension is being triggered by EXT022MI/AddPDS022
 *
 * Name: AddPDS022
 * Description: XTend made to insert all the enreg in MPDSST 
 * Date       Changed By                     Description
 * 20231013   Ludovic TRAVERS                Create EXT022MI_AddPDS022
 */
 
public class AddPDS022 extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  
  public AddPDS022(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.utility = utility
  }
  
  int cono
  String faci
  String plgr
  String chid
  char cktp
  String cmky
  String csty
  char pktp
  String pmky
  String psty
  
  String fdatS
  String tdatS
  int fdat
  int tdat
  int fdatC
  int tdatC
  
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
    
    DBAction query = database.table("MPDSST")
      .index("00")
      .build()
    
    DBContainer container = query.getContainer()
    
    container.set("PTCONO", cono)
    
    faci = mi.inData.get("FACI").trim()
    
    if (faci.isEmpty()) {
       mi.error("Etablissement est obligatoire")
       return
    }
    
    plgr = mi.inData.get("PLGR").trim()
    
    if (plgr.isEmpty()) {
       mi.error("Poste de charge est obligatoire")
       return
    }
    
    if (!checkWorkCenterValidity(faci, plgr)) {
      return
    }
    
    container.set("PTFACI", mi.inData.get("FACI").trim())
    container.set("PTPLGR", mi.inData.get("PLGR").trim())
    
    cktp = mi.inData.get("CKTP").charAt(0)
    if ( (cktp != '1') && (cktp != '2') ) {
      mi.error("Current MO key type $cktp is invalid")
      return
    }
    
    cmky = mi.inData.get("CMKY").trim()
    csty = mi.inData.get("CSTY").trim()
    if (cktp == '1') {
      if (!checkPDS001(faci, cmky, csty)) {
        return
      }
    }
    
    if (cktp == '2') {
      if (!csty.isEmpty()) {
        mi.error("Structure type should not be entered")
        return
      }
    }
    
    if (!mi.inData.get("CKTP").trim().isEmpty()) {
      container.set("PTCKTP", mi.inData.get("CKTP").charAt(0))
    }
    else {
      container.set("PTCKTP", ' ')
    }
    
    container.set("PTCMKY", mi.inData.get("CMKY").trim())
    
    container.set("PTCSTY", mi.inData.get("CSTY").trim())
   
    pktp = mi.inData.get("PKTP").charAt(0)
    if ( (pktp != ' ') && (pktp != '1') && (pktp != '2') ) {
      mi.error("Previous MO key type $pktp is invalid")
      return
    }
    
    pmky = mi.inData.get("PMKY").trim()
    psty = mi.inData.get("PSTY").trim()
    if (pktp == ' ') {
      if (!pmky.isEmpty() || !psty.isEmpty()) {
        mi.error("Previous MO key type $pktp is invalid")
        return
      }
    }
    
    if (pktp == '1') {
      if (!checkPDS001(faci, pmky, psty)) {
        return
      }
    }
    
    if (pktp == '2') {
      if (!psty.isEmpty()) {
        mi.error("Structure type should not be entered")
        return
      }
    }
    
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
    
    if (!utility.call("DateUtil","isDateValid", fdatS, "yyyyMMdd")) {
       mi.error("Date de début $fdatS n'est pas valide")
       return
    }
    
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
     
      if (!checkDatesValidity(fdat, tdat)) {
        return
      }
    }
    
    container.set("PTFDAT", fdat)
    
    if (!query.read(container)) {
      container.set("PTTDAT", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("TDAT")))
      
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
      
      container.set("PTSETI", utility.call("NumberUtil","parseStringToDouble", mi.inData.get("SETI")))
      container.set("PTMXID", mi.inData.get("MXID").trim())
      container.set("PTCMAC", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("CMAC")))
      container.set("PTPMAC", utility.call("NumberUtil","parseStringToInteger", mi.inData.get("PMAC")))
      if (!mi.inData.get("SETT").trim().isEmpty()) {
        container.set("PTSETT", mi.inData.get("SETT").charAt(0))
      }
      else {
        container.set("PTSETT", ' ')
      }
     
      container.set("PTRGDT", utility.call("DateUtil","currentDateY8AsInt"))
      container.set("PTRGTM", utility.call("DateUtil","currentTimeAsInt"))
      container.set("PTLMDT", utility.call("DateUtil","currentDateY8AsInt"))
      container.set("PTCHNO", 1)
      container.set("PTCHID", chid)
      
      query.insert(container)
    } else {
      mi.error("Enregistrement déjà existant dans la table MPDSST.")
      return
    }
  }
  
  /**
  * On vérifie si le FACI passé en paramètre d'entrée existe en CRS008=CFACIL
  */
  private boolean checkFacilityExist(String FACI) {
    DBAction query = database.table("CFACIL")
      .index("00")
      .build()
    DBContainer container = query.getContainer()
    container.set("CFCONO", cono)
    container.set("CFFACI", FACI)
    
    return query.read(container)
  }
  
  /**
  * On vérifie si le PLGR passé en paramètre d'entrée existe en PDS010=MPDWCT
  */
  private boolean checkWorkCenterValidity(String FACI, String PLGR) {
    
    char PLTP = ' '
    int APSA = 0
    
    DBAction query1 = database.table("CFACIL")
      .index("00")
      .selection("CFAPSA")
      .build()
    DBContainer container1 = query1.getContainer()
    container1.set("CFCONO", cono)
    container1.set("CFFACI", FACI)
    if (!query1.read(container1)) {
      mi.error("Etablissement $FACI inexistant en CRS008")
      return false
    }
    else {
      APSA = container1.get("CFAPSA")
    }
    
    DBAction query2 = database.table("MPDWCT")
      .index("00")
      .selection("PPPLTP")
      .build()
    DBContainer container2 = query2.getContainer()
    container2.set("PPCONO", cono)
    container2.set("PPFACI", FACI)
    container2.set("PPPLGR", PLGR)
    if (!query2.read(container2)) {
      mi.error("WorkCenter $PLGR inexistant en PDS010")
      return false
    }
    else {
      PLTP = container2.get("PPPLTP")
    }
    
    if ( (PLTP != '1') && (PLTP != '2') && (PLTP != '6') ) {
       mi.error("Work center has resource type $PLTP - entry not permitted")
       return false
    }
    if (APSA == 1) {
      if (PLTP != '6') {
        mi.error("APP is active - resource type $PLTP invalid")
        return false
      }
    }
    return true
  }
  
  /**
  * On vérifie si le produit passé en paramètre d'entrée existe en PDS001 = MPDHED
  */
  private boolean checkPDS001(String FACI, String PRNO, String STRT) {
    
    DBAction query = database.table("MPDHED")
      .index("00")
      .build()
    DBContainer container = query.getContainer()
    container.set("PHCONO", cono)
    container.set("PHFACI", FACI)
    container.set("PHPRNO", PRNO)
    container.set("PHSTRT", STRT)
    if (!query.read(container)) {
      mi.error("Produit $PRNO inexistant en PDS001")
      return false
    }
    return true
  } 
  
  /**
  * On vérifie si les intervalles de date sont valides
  */
  private boolean checkDatesValidity(int FDAT, int TDAT) {
    
    boolean OK
    OK = true
    
    DBAction query = database.table("MPDSST")
      .index("00")
      .selection("PTFDAT", "PTTDAT")
      .build()
    
    DBContainer container = query.getContainer()
    container.set("PTCONO", cono)
    container.set("PTFACI", faci)
    container.set("PTPLGR", plgr)
    container.set("PTCMKY", cmky)
    container.set("PTCKTP", cktp)
    container.set("PTCSTY", csty)
    container.set("PTPMKY", pmky)
    container.set("PTPKTP", pktp)
    container.set("PTPSTY", psty)
    
    Closure<?> releasedItemProcessor = {
      DBContainer data ->
      fdatC = data.get("PTFDAT")
      tdatC = data.get("PTTDAT")
      if ((fdatC <= FDAT) && (tdatC >= FDAT)) {
        mi.error("From date is inside another record´s interval")
        OK = false
        return false
      }
    }
    
    query.readAll(container, 9, releasedItemProcessor)
    return OK
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