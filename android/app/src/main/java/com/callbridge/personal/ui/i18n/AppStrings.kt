package com.callbridge.personal.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.callbridge.personal.ui.theme.AppLanguage

data class I18nStrings(
    // Bottom Tabs
    val tabDashboard: String,
    val tabContacts: String,
    val tabHistory: String,
    val tabSettings: String,

    // Common Actions
    val actionNewCall: String,
    val actionNewMessage: String,
    val actionCall: String,
    val actionMessage: String,
    val actionCancel: String,
    val actionSave: String,
    val actionClose: String,
    val actionSend: String,
    val actionSearch: String,
    val actionDelete: String,

    // Dashboard
    val dashboardBalanceTitle: String,
    val dashboardActiveCallerId: String,
    val dashboardContactsDesc: String,
    val dashboardHistoryDesc: String,
    val dashboardRecentCalls: String,
    val dashboardViewAll: String,
    val dashboardMissed: String,
    val dashboardOutgoing: String,
    val dashboardIncoming: String,

    // Dialpad
    val dialpadTitle: String,
    val dialpadDirectBadge: String,
    val dialpadPlaceholder: String,
    val dialpadSelectCountry: String,
    val dialpadSearchCountryPlaceholder: String,

    // Active Call
    val callAuthenticating: String,
    val callConnecting: String,
    val callConnected: String,
    val callEnded: String,
    val callMute: String,
    val callSpeaker: String,
    val callKeypad: String,
    val callNotes: String,
    val callEndButton: String,

    // Messages
    val messagesTitle: String,
    val messagesSubtitle: String,
    val messagesSearchPlaceholder: String,
    val messagesNoMessagesTitle: String,
    val messagesStartConversation: String,
    val messagesQuickTemplates: String,
    val messagesInputPlaceholder: String,
    val messagesDelivered: String,

    // Settings
    val settingsTitle: String,
    val settingsLanguageTitle: String,
    val settingsThemeTitle: String,
    val settingsDialSoundTitle: String,
    val settingsCallerIdTitle: String,
    val settingsLiveBalance: String,
    val settingsRefreshBalance: String,
    val settingsAboutTitle: String
)

object AppStrings {
    private val English = I18nStrings(
        tabDashboard = "Dashboard",
        tabContacts = "Contacts",
        tabHistory = "History",
        tabSettings = "Settings",

        actionNewCall = "New Call",
        actionNewMessage = "New Message",
        actionCall = "Call",
        actionMessage = "Send SMS",
        actionCancel = "Cancel",
        actionSave = "Save",
        actionClose = "Close",
        actionSend = "Send",
        actionSearch = "Search",
        actionDelete = "Delete",

        dashboardBalanceTitle = "Twilio Account Balance",
        dashboardActiveCallerId = "Active Caller ID (Worldwide Dialing)",
        dashboardContactsDesc = "Personal CRM",
        dashboardHistoryDesc = "Call activity",
        dashboardRecentCalls = "Recent Calls",
        dashboardViewAll = "View All",
        dashboardMissed = "Missed",
        dashboardOutgoing = "Outgoing",
        dashboardIncoming = "Incoming",

        dialpadTitle = "Dial Pad",
        dialpadDirectBadge = "CallBridge Direct",
        dialpadPlaceholder = "Enter phone number...",
        dialpadSelectCountry = "Select Destination Country",
        dialpadSearchCountryPlaceholder = "Search country or code...",

        callAuthenticating = "Authenticating Twilio VoIP...",
        callConnecting = "Connecting...",
        callConnected = "Call Connected",
        callEnded = "Call Ended",
        callMute = "Mute",
        callSpeaker = "Speaker",
        callKeypad = "Keypad",
        callNotes = "Notes",
        callEndButton = "End Call",

        messagesTitle = "Messages",
        messagesSubtitle = "CallBridge SMS Gateway",
        messagesSearchPlaceholder = "Search messages or numbers...",
        messagesNoMessagesTitle = "No Messages Yet",
        messagesStartConversation = "Start Conversation",
        messagesQuickTemplates = "Quick SMS Templates",
        messagesInputPlaceholder = "CallBridge SMS...",
        messagesDelivered = "• Delivered",

        settingsTitle = "Settings",
        settingsLanguageTitle = "App Language",
        settingsThemeTitle = "Visual Theme",
        settingsDialSoundTitle = "Keypad Dial Tones",
        settingsCallerIdTitle = "Active Caller ID",
        settingsLiveBalance = "Live Account Balance",
        settingsRefreshBalance = "Refresh",
        settingsAboutTitle = "About CallBridge"
    )

    private val Bengali = I18nStrings(
        tabDashboard = "ড্যাশবোর্ড",
        tabContacts = "কন্টাক্টস",
        tabHistory = "হিস্ট্রি",
        tabSettings = "সেটিংস",

        actionNewCall = "নতুন কল",
        actionNewMessage = "নতুন মেসেজ",
        actionCall = "কল",
        actionMessage = "এসএমএস পাঠান",
        actionCancel = "বাতিল",
        actionSave = "সংরক্ষণ",
        actionClose = "বন্ধ করুন",
        actionSend = "পাঠান",
        actionSearch = "অনুসন্ধান",
        actionDelete = "মুছুন",

        dashboardBalanceTitle = "টুইলিও অ্যাকাউন্ট ব্যালেন্স",
        dashboardActiveCallerId = "সক্রিয় কলার আইডি (গ্লোবাল ডায়ালিং)",
        dashboardContactsDesc = "ব্যক্তিগত কন্টাক্টস",
        dashboardHistoryDesc = "কল অ্যাক্টিভিটি",
        dashboardRecentCalls = "সাম্প্রতিক কলসমূহ",
        dashboardViewAll = "সব দেখুন",
        dashboardMissed = "মিসড",
        dashboardOutgoing = "আউটগোয়িং",
        dashboardIncoming = "ইনকামিং",

        dialpadTitle = "ডায়াল প্যাড",
        dialpadDirectBadge = "কলব্রিজ ডিরেক্ট",
        dialpadPlaceholder = "ফোন নম্বর টাইপ করুন...",
        dialpadSelectCountry = "গন্তব্য দেশ নির্বাচন করুন",
        dialpadSearchCountryPlaceholder = "দেশের নাম বা কোড খুঁজুন...",

        callAuthenticating = "টুইলিও ভিওআইপি সংযোগ হচ্ছে...",
        callConnecting = "কল যুক্ত হচ্ছে...",
        callConnected = "কল সংযুক্ত হয়েছে",
        callEnded = "কল সমাপ্ত",
        callMute = "মিউট",
        callSpeaker = "স্পিকার",
        callKeypad = "কিপ্যাড",
        callNotes = "নোটস",
        callEndButton = "কল কাটুন",

        messagesTitle = "মেসেজ",
        messagesSubtitle = "কলব্রিজ এসএমএস গেটওয়ে",
        messagesSearchPlaceholder = "মেসেজ বা নম্বর খুঁজুন...",
        messagesNoMessagesTitle = "এখনো কোনো মেসেজ নেই",
        messagesStartConversation = "মেসেজ পাঠানো শুরু করুন",
        messagesQuickTemplates = "কুইক এসএমএস টেমপ্লেট",
        messagesInputPlaceholder = "কলব্রিজ এসএমএস...",
        messagesDelivered = "• ডেলিভার্ড",

        settingsTitle = "সেটিংস",
        settingsLanguageTitle = "অ্যাপের ভাষা (Language)",
        settingsThemeTitle = "ভিজ্যুয়াল থিম",
        settingsDialSoundTitle = "কিপ্যাড ডায়াল টোন",
        settingsCallerIdTitle = "সক্রিয় কলার আইডি",
        settingsLiveBalance = "লাইভ অ্যাকাউন্ট ব্যালেন্স",
        settingsRefreshBalance = "রিফ্রেশ",
        settingsAboutTitle = "কলব্রিজ পরিচিতি"
    )

    private val Hindi = I18nStrings(
        tabDashboard = "डैशबोर्ड",
        tabContacts = "संपर्क",
        tabHistory = "इतिहास",
        tabSettings = "सेटिंग्स",

        actionNewCall = "नई कॉल",
        actionNewMessage = "नया संदेश",
        actionCall = "कॉल",
        actionMessage = "एसएमएस भेजें",
        actionCancel = "रद्द करें",
        actionSave = "सहेजें",
        actionClose = "बंद करें",
        actionSend = "भेजें",
        actionSearch = "खोजें",
        actionDelete = "हटाएं",

        dashboardBalanceTitle = "ट्विलियो खाता शेष",
        dashboardActiveCallerId = "सक्रिय कॉलर आईडी (वैश्विक डायलिंग)",
        dashboardContactsDesc = "व्यक्तिगत संपर्क",
        dashboardHistoryDesc = "कॉल विवरण",
        dashboardRecentCalls = "हाल की कॉलें",
        dashboardViewAll = "सभी देखें",
        dashboardMissed = "मिस्ड",
        dashboardOutgoing = "आउटगोइंग",
        dashboardIncoming = "इनकमिंग",

        dialpadTitle = "डायल पैड",
        dialpadDirectBadge = "कॉलब्रिज डायरेक्ट",
        dialpadPlaceholder = "फ़ोन नंबर दर्ज करें...",
        dialpadSelectCountry = "गंतव्य देश चुनें",
        dialpadSearchCountryPlaceholder = "देश का नाम या कोड खोजें...",

        callAuthenticating = "ट्विलियो वीओआईपी प्रमाणीकरण...",
        callConnecting = "कॉल कनेक्ट हो रही है...",
        callConnected = "कॉल कनेक्टेड",
        callEnded = "कॉल समाप्त",
        callMute = "म्यूट",
        callSpeaker = "स्पीकर",
        callKeypad = "कीपैड",
        callNotes = "नोट्स",
        callEndButton = "कॉल काटें",

        messagesTitle = "संदेश",
        messagesSubtitle = "कॉलब्रिज एसएमएस गेटवे",
        messagesSearchPlaceholder = "संदेश या नंबर खोजें...",
        messagesNoMessagesTitle = "अभी कोई संदेश नहीं है",
        messagesStartConversation = "बातचीत शुरू करें",
        messagesQuickTemplates = "त्वरित एसएमएस टेम्पलेट",
        messagesInputPlaceholder = "कॉलब्रिज एसएमएस...",
        messagesDelivered = "• वितरित",

        settingsTitle = "सेटिंग्स",
        settingsLanguageTitle = "ऐप की भाषा",
        settingsThemeTitle = "थीम चयन",
        settingsDialSoundTitle = "कीपैड डायलर टोन",
        settingsCallerIdTitle = "सक्रिय कॉलर आईडी",
        settingsLiveBalance = "लाइव खाता शेष",
        settingsRefreshBalance = "रीफ्रेश",
        settingsAboutTitle = "कॉलब्रिज के बारे में"
    )

    private val Spanish = I18nStrings(
        tabDashboard = "Panel",
        tabContacts = "Contactos",
        tabHistory = "Historial",
        tabSettings = "Ajustes",

        actionNewCall = "Nueva Llamada",
        actionNewMessage = "Nuevo Mensaje",
        actionCall = "Llamar",
        actionMessage = "Enviar SMS",
        actionCancel = "Cancelar",
        actionSave = "Guardar",
        actionClose = "Cerrar",
        actionSend = "Enviar",
        actionSearch = "Buscar",
        actionDelete = "Eliminar",

        dashboardBalanceTitle = "Saldo de Cuenta Twilio",
        dashboardActiveCallerId = "Identificador de Llamadas Activo",
        dashboardContactsDesc = "CRM Personal",
        dashboardHistoryDesc = "Actividad de llamadas",
        dashboardRecentCalls = "Llamadas Recientes",
        dashboardViewAll = "Ver Todo",
        dashboardMissed = "Perdida",
        dashboardOutgoing = "Saliente",
        dashboardIncoming = "Entrante",

        dialpadTitle = "Teclado",
        dialpadDirectBadge = "CallBridge Directo",
        dialpadPlaceholder = "Ingresar número telefónico...",
        dialpadSelectCountry = "Seleccionar País de Destino",
        dialpadSearchCountryPlaceholder = "Buscar país o código...",

        callAuthenticating = "Autenticando VoIP Twilio...",
        callConnecting = "Conectando llamada...",
        callConnected = "Llamada Conectada",
        callEnded = "Llamada Finalizada",
        callMute = "Silenciar",
        callSpeaker = "Altavoz",
        callKeypad = "Teclado",
        callNotes = "Notas",
        callEndButton = "Finalizar",

        messagesTitle = "Mensajes",
        messagesSubtitle = "Pasarela SMS CallBridge",
        messagesSearchPlaceholder = "Buscar mensajes o números...",
        messagesNoMessagesTitle = "Aún No Hay Mensajes",
        messagesStartConversation = "Iniciar Conversación",
        messagesQuickTemplates = "Plantillas Rápidas",
        messagesInputPlaceholder = "SMS de CallBridge...",
        messagesDelivered = "• Entregado",

        settingsTitle = "Ajustes",
        settingsLanguageTitle = "Idioma de la Aplicación",
        settingsThemeTitle = "Tema Visual",
        settingsDialSoundTitle = "Tonos de Teclado DTMF",
        settingsCallerIdTitle = "Identificador Activo",
        settingsLiveBalance = "Saldo en Tiempo Real",
        settingsRefreshBalance = "Actualizar",
        settingsAboutTitle = "Acerca de CallBridge"
    )

    private val Arabic = I18nStrings(
        tabDashboard = "لوحة القيادة",
        tabContacts = "جهات الاتصال",
        tabHistory = "السجل",
        tabSettings = "الإعدادات",

        actionNewCall = "مكالمة جديدة",
        actionNewMessage = "رسالة جديدة",
        actionCall = "اتصال",
        actionMessage = "إرسال رسالة",
        actionCancel = "إلغاء",
        actionSave = "حفظ",
        actionClose = "إغلاق",
        actionSend = "إرسال",
        actionSearch = "بحث",
        actionDelete = "حذف",

        dashboardBalanceTitle = "رصيد حساب تويليو",
        dashboardActiveCallerId = "معرف المتصل النشط (اتصال دولي)",
        dashboardContactsDesc = "سجل العناوين الشخصي",
        dashboardHistoryDesc = "نشاط المكالمات",
        dashboardRecentCalls = "المكالمات الأخيرة",
        dashboardViewAll = "عرض الكل",
        dashboardMissed = "فائتة",
        dashboardOutgoing = "صادرة",
        dashboardIncoming = "واردة",

        dialpadTitle = "لوحة الاتصال",
        dialpadDirectBadge = "كول بريدج المباشر",
        dialpadPlaceholder = "أدخل رقم الهاتف...",
        dialpadSelectCountry = "اختر دولة الوجهة",
        dialpadSearchCountryPlaceholder = "ابحث عن الدولة أو الرمز...",

        callAuthenticating = "جاري مصادقة خدمة الصوت...",
        callConnecting = "جاري الاتصال...",
        callConnected = "المكالمة متصلة",
        callEnded = "انتهت المكالمة",
        callMute = "كتم الصوت",
        callSpeaker = "مكبر الصوت",
        callKeypad = "لوحة المفاتيح",
        callNotes = "ملاحظات",
        callEndButton = "إنهاء المكالمة",

        messagesTitle = "الرسائل",
        messagesSubtitle = "بوابة كول بريدج للرسائل القصيرة",
        messagesSearchPlaceholder = "ابحث في الرسائل أو الأرقام...",
        messagesNoMessagesTitle = "لا توجد رسائل بعد",
        messagesStartConversation = "بدء محادثة",
        messagesQuickTemplates = "قوالب الرسائل السريعة",
        messagesInputPlaceholder = "رسالة كول بريدج...",
        messagesDelivered = "• تم التسليم",

        settingsTitle = "الإعدادات",
        settingsLanguageTitle = "لغة التطبيق",
        settingsThemeTitle = "المظهر",
        settingsDialSoundTitle = "نغمات لوحة الاتصال",
        settingsCallerIdTitle = "معرف المتصل النشط",
        settingsLiveBalance = "الرصيد المباشر",
        settingsRefreshBalance = "تحديث",
        settingsAboutTitle = "حول كول بريدج"
    )

    private val Portuguese = I18nStrings(
        tabDashboard = "Painel",
        tabContacts = "Contatos",
        tabHistory = "Histórico",
        tabSettings = "Ajustes",

        actionNewCall = "Nova Chamada",
        actionNewMessage = "Nova Mensagem",
        actionCall = "Ligar",
        actionMessage = "Enviar SMS",
        actionCancel = "Cancelar",
        actionSave = "Salvar",
        actionClose = "Fechar",
        actionSend = "Enviar",
        actionSearch = "Buscar",
        actionDelete = "Excluir",

        dashboardBalanceTitle = "Saldo da Conta Twilio",
        dashboardActiveCallerId = "ID de Chamada Ativo (Discagem Global)",
        dashboardContactsDesc = "CRM Pessoal",
        dashboardHistoryDesc = "Atividade de chamadas",
        dashboardRecentCalls = "Chamadas Recentes",
        dashboardViewAll = "Ver Tudo",
        dashboardMissed = "Perdida",
        dashboardOutgoing = "Efetuada",
        dashboardIncoming = "Recebida",

        dialpadTitle = "Teclado",
        dialpadDirectBadge = "CallBridge Direto",
        dialpadPlaceholder = "Digite o número de telefone...",
        dialpadSelectCountry = "Selecione o País de Destino",
        dialpadSearchCountryPlaceholder = "Buscar país ou código...",

        callAuthenticating = "Autenticando VoIP Twilio...",
        callConnecting = "Conectando chamada...",
        callConnected = "Chamada Conectada",
        callEnded = "Chamada Encerrada",
        callMute = "Mudo",
        callSpeaker = "Alto-falante",
        callKeypad = "Teclado",
        callNotes = "Notas",
        callEndButton = "Encerrar",

        messagesTitle = "Mensagens",
        messagesSubtitle = "Gateway SMS CallBridge",
        messagesSearchPlaceholder = "Buscar mensagens ou números...",
        messagesNoMessagesTitle = "Nenhuma Mensagem Ainda",
        messagesStartConversation = "Iniciar Conversa",
        messagesQuickTemplates = "Modelos Rápidos",
        messagesInputPlaceholder = "SMS CallBridge...",
        messagesDelivered = "• Entregue",

        settingsTitle = "Ajustes",
        settingsLanguageTitle = "Idioma do Aplicativo",
        settingsThemeTitle = "Tema Visual",
        settingsDialSoundTitle = "Sons do Teclado DTMF",
        settingsCallerIdTitle = "ID de Chamada Ativo",
        settingsLiveBalance = "Saldo em Tempo Real",
        settingsRefreshBalance = "Atualizar",
        settingsAboutTitle = "Sobre o CallBridge"
    )

    private val Russian = I18nStrings(
        tabDashboard = "Панель",
        tabContacts = "Контакты",
        tabHistory = "История",
        tabSettings = "Настройки",

        actionNewCall = "Новый вызов",
        actionNewMessage = "Новое сообщение",
        actionCall = "Позвонить",
        actionMessage = "Отправить SMS",
        actionCancel = "Отмена",
        actionSave = "Сохранить",
        actionClose = "Закрыть",
        actionSend = "Отправить",
        actionSearch = "Поиск",
        actionDelete = "Удалить",

        dashboardBalanceTitle = "Баланс учетной записи Twilio",
        dashboardActiveCallerId = "Активный Caller ID (Международный)",
        dashboardContactsDesc = "Личные контакты",
        dashboardHistoryDesc = "Журнал звонков",
        dashboardRecentCalls = "Недавние звонки",
        dashboardViewAll = "Смотреть все",
        dashboardMissed = "Пропущенный",
        dashboardOutgoing = "Исходящий",
        dashboardIncoming = "Входящий",

        dialpadTitle = "Клавиатура",
        dialpadDirectBadge = "CallBridge Direct",
        dialpadPlaceholder = "Введите номер телефона...",
        dialpadSelectCountry = "Выберите страну назначения",
        dialpadSearchCountryPlaceholder = "Поиск страны или кода...",

        callAuthenticating = "Авторизация VoIP Twilio...",
        callConnecting = "Соединение...",
        callConnected = "Вызов соединен",
        callEnded = "Вызов завершен",
        callMute = "Микрофон",
        callSpeaker = "Динамик",
        callKeypad = "Клавиши",
        callNotes = "Заметки",
        callEndButton = "Завершить",

        messagesTitle = "Сообщения",
        messagesSubtitle = "SMS-шлюз CallBridge",
        messagesSearchPlaceholder = "Поиск сообщений или номеров...",
        messagesNoMessagesTitle = "Сообщений пока нет",
        messagesStartConversation = "Начать диалог",
        messagesQuickTemplates = "Быстрые шаблоны",
        messagesInputPlaceholder = "SMS CallBridge...",
        messagesDelivered = "• Доставлено",

        settingsTitle = "Настройки",
        settingsLanguageTitle = "Язык приложения",
        settingsThemeTitle = "Тема оформления",
        settingsDialSoundTitle = "Звуки клавиш DTMF",
        settingsCallerIdTitle = "Активный Caller ID",
        settingsLiveBalance = "Текущий баланс",
        settingsRefreshBalance = "Обновить",
        settingsAboutTitle = "О приложении CallBridge"
    )

    private val Chinese = I18nStrings(
        tabDashboard = "控制台",
        tabContacts = "联系人",
        tabHistory = "通话记录",
        tabSettings = "设置",

        actionNewCall = "拨打新电话",
        actionNewMessage = "发送新消息",
        actionCall = "拨打",
        actionMessage = "发送短信",
        actionCancel = "取消",
        actionSave = "保存",
        actionClose = "关闭",
        actionSend = "发送",
        actionSearch = "搜索",
        actionDelete = "删除",

        dashboardBalanceTitle = "Twilio 账户余额",
        dashboardActiveCallerId = "当前主叫号码（全球呼叫）",
        dashboardContactsDesc = "个人通讯录",
        dashboardHistoryDesc = "近期通话活动",
        dashboardRecentCalls = "最近通话",
        dashboardViewAll = "查看全部",
        dashboardMissed = "未接",
        dashboardOutgoing = "呼出",
        dashboardIncoming = "呼入",

        dialpadTitle = "拨号盘",
        dialpadDirectBadge = "CallBridge 直连",
        dialpadPlaceholder = "输入电话号码...",
        dialpadSelectCountry = "选择目标国家/地区",
        dialpadSearchCountryPlaceholder = "搜索国家或区号...",

        callAuthenticating = "正在验证 Twilio VoIP...",
        callConnecting = "正在连接电话...",
        callConnected = "通话已接通",
        callEnded = "通话已结束",
        callMute = "静音",
        callSpeaker = "免提",
        callKeypad = "键盘",
        callNotes = "备忘录",
        callEndButton = "挂断",

        messagesTitle = "短信消息",
        messagesSubtitle = "CallBridge 短信网关",
        messagesSearchPlaceholder = "搜索消息或号码...",
        messagesNoMessagesTitle = "暂无消息记录",
        messagesStartConversation = "发起对话",
        messagesQuickTemplates = "快捷短信模板",
        messagesInputPlaceholder = "CallBridge 短信内容...",
        messagesDelivered = "• 已送达",

        settingsTitle = "应用设置",
        settingsLanguageTitle = "系统语言 (Language)",
        settingsThemeTitle = "界面主题",
        settingsDialSoundTitle = "按键拨号音 (DTMF)",
        settingsCallerIdTitle = "主叫号码显示",
        settingsLiveBalance = "实时账户余额",
        settingsRefreshBalance = "刷新余额",
        settingsAboutTitle = "关于 CallBridge"
    )

    fun forLanguage(language: AppLanguage): I18nStrings = when (language) {
        AppLanguage.ENGLISH -> English
        AppLanguage.BENGALI -> Bengali
        AppLanguage.HINDI -> Hindi
        AppLanguage.SPANISH -> Spanish
        AppLanguage.ARABIC -> Arabic
        AppLanguage.PORTUGUESE -> Portuguese
        AppLanguage.RUSSIAN -> Russian
        AppLanguage.CHINESE -> Chinese
    }
}

val LocalAppStrings = staticCompositionLocalOf {
    AppStrings.forLanguage(AppLanguage.ENGLISH)
}
