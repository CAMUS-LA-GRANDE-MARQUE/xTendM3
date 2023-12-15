/**
 * README
 * This extension is being triggered by EXT022MI/DelPDS022
 *
 * Name: DelPDS022
 * Description: XTend made to delete an enreg in MPDSST 
 * Date       Changed By                     Description
 * 20231013   Ludovic TRAVERS                Create EXT022MI_DelPDS022
 */
 
public class DelPDS022 extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  
  public DelPDS022(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.utility = utility
  }
  
  int cono
  int fdat
  
  public void main() {
    cono = (Integer) program.getLDAZD().CONO
    if (!mi.inData.get("CONO").trim().isEmpty()) {
      cono = utility.call("NumberUtil","parseStringToInteger", mi.inData.get("CONO"))
    }
    
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
   
    container.set("PTFDAT", fdat)
    
    Closure<?> deleterCallback = { LockedResult lockedResult ->
      lockedResult.delete()
    }
  
    if (!query.readLock(container, deleterCallback)) {
      mi.error("L'enregistrement n'existe pas.")
    }
  }
}