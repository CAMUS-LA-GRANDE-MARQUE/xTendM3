/**
 * README
 * Création enregistrement dans la table MITPOP (programme MMS025) en spécifique pour ne pas tenir compte de l'unicité des EAN13 dans l'API standard.
 * Cette API sera utilisée exclusiement dans un mashup. Celui-ci vérifie la validité de la clé de la référence complémentaire.
 *
 * Name: AddAlias
 * Description: 
 * Date       Changed By                     Description
 * 20231221   François Leprévost             Création verbe AddAlias spécifique pour MMS025
 * 20240103   François Leprévost             On teste si les champs d'entrée ont la valeur null, sinon le .trim() plante lors de l'appel de l'API par EVS100
 */
public class AddAlias extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  
  public AddAlias(MIAPI mi, DatabaseAPI database , ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
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
    String alwt = ""
    if (mi.inData.get("ALWT") != null) {
      alwt = mi.inData.get("ALWT").trim()
    }
    
    String alwq = ""
    if (mi.inData.get("ALWQ") != null) {
      alwq = mi.inData.get("ALWQ").trim()
    }
    
    String itno = ""
    if (mi.inData.get("ITNO") != null) {
      itno = mi.inData.get("ITNO").trim()
    }
    String popn = ""
    if (mi.inData.get("POPN") != null) {
      popn = mi.inData.get("POPN").trim()
    }
    String vfdt = ""
    if (mi.inData.get("VFDT") != null) {
      vfdt = mi.inData.get("VFDT").trim()
    }
    String alun = ""
    if (mi.inData.get("ALUN") != null) {
      alun = mi.inData.get("ALUN").trim()
    }
    
    if (alwt.isEmpty()) {
      mi.error("La catgéorie est obligatoire.")
      return
    } else if (!utility.call("NumberUtil","isValidNumber", alwt, ".")) {
      mi.error("La catgéorie doit être numérique.")
      return
    }
    
    if (itno.isEmpty()) {
      mi.error("Le code article est obligatoire.")
      return
    } else if (!checkItemExist(itno)) {
      mi.error("Le code article est inexistant.")
      return
    }
    
    if (popn.isEmpty()) {
      mi.error("La référence complémentaire est obligatoire.")
      return
    }
    
    if (!utility.call("NumberUtil","isValidNumber", vfdt, ".")) {
      mi.error("La date de début doit être numérique.")
      return
    } else if(!utility.call("DateUtil", "isDateValid", vfdt, "yyyyMMdd")) {
      mi.error("La date de début est invalide.")
      return
    }
    
    if (!createEnreg(alwt, alwq, itno, popn, vfdt, alun)) {
      mi.error("L'enregistrement existe déjà !")
      return
    }
  }
  
  /**
  * On vérifie si l'article existe.
  */
  private boolean checkItemExist(String itno) {
    DBAction query = database.table("MITMAS").index("00").build()
    DBContainer container = query.getContainer()
    container.set("MMCONO", cono)
    container.set("MMITNO", itno)
    
    return query.read(container)
  }
  
  /**
  * Création de l'enregistrement si et seulement si celui-ci n'existe pas.
  */  
  private boolean createEnreg(String alwt, String alwq, String itno, String popn, String vfdt, String alun) {
    DBAction query = database.table("MITPOP").index("00").build()
    DBContainer container = query.getContainer()
    
    container.set("MPCONO", cono)
    container.set("MPALWT", utility.call("NumberUtil","parseStringToInteger", alwt))
    container.set("MPCONO", cono)
    container.set("MPALWQ", alwq)
    container.set("MPITNO", itno)
    container.set("MPPOPN", popn)
    container.set("MPE0PA", "")
    container.set("MPSEA1", "")
    container.set("MPVFDT", utility.call("NumberUtil","parseStringToInteger", vfdt))
    
    if (!query.read(container)) {
      container.set("MPALUN", alun)
      container.set("MPRGDT", utility.call("DateUtil","currentDateY8AsInt"))
      container.set("MPRGTM", utility.call("DateUtil","currentTimeAsInt"))
      container.set("MPLMDT", utility.call("DateUtil","currentDateY8AsInt"))
      container.set("MPCHNO", 1)
      container.set("MPCHID", chid)
      
      query.insert(container)
      return true
    } else {
      return false
    }
  }
}