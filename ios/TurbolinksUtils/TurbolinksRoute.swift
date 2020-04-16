import Turbolinks

class TurbolinksRoute {
    var title: String?
    var subtitle: String?
    var action: Action?
    var url: URL?
    var component: String?
    var modal: Bool = false
    var dismissable: Bool = false
    var passProps: Dictionary<AnyHashable, Any>?
    var actions: Array<Dictionary<AnyHashable, Any>>?
    var leftButtonIcon: UIImage?
    var leftButtonText: String?
    var rightButtonIcon: UIImage?
    var rightButtonText: String?
    var hidesShadow: Bool = false
    var hidesNavBar: Bool = false
    var visibleDropDown: Bool = false
    var animated: Bool = true
    var trasparent: CGFloat = 0.0

    init(_ route: Dictionary<AnyHashable, Any>) {
        self.title = RCTConvert.nsString(route["title"])
        self.subtitle = RCTConvert.nsString(route["subtitle"])
        self.action = Action(rawValue: RCTConvert.nsString(route["action"]) ?? "advance")
        self.url = RCTConvert.nsurl(route["url"])
        self.component = RCTConvert.nsString(route["component"])
        self.modal = RCTConvert.bool(route["modal"])
        self.dismissable = RCTConvert.bool(route["dismissable"])
        self.passProps = RCTConvert.nsDictionary(route["passProps"])
        self.actions = RCTConvert.nsDictionaryArray(route["actions"])
        self.leftButtonIcon = RCTConvert.uiImage(route["leftButtonIcon"])
        self.leftButtonText = RCTConvert.nsString(route["leftButtonText"])
        self.rightButtonIcon = RCTConvert.uiImage(route["rightButtonIcon"])
        self.rightButtonText = RCTConvert.nsString(route["rightButtonText"])
        self.hidesShadow = RCTConvert.bool(route["hidesShadow"])
        self.hidesNavBar = RCTConvert.bool(route["hidesNavBar"])
        self.visibleDropDown = RCTConvert.bool(route["visibleDropDown"])
        self.animated = RCTConvert.bool(route["animated"])
        self.trasparent = RCTConvert.cgFloat(route["trasparent"])

    }
}
