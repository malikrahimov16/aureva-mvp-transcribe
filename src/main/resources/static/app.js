'use strict';

/* =========================================================
   AUREVA — Frontend Application
   Загрузка, транскрипция, запись, история, настройки.
   ========================================================= */

document.addEventListener('DOMContentLoaded', () => {

    // 1. DOM ELEMENTS

    const elements = {
        fileInput: document.getElementById('mediaFile'),
        dropzone: document.getElementById('file-dropzone'),
        selectedFileName: document.getElementById('selectedFileName'),
        transcribeButton: document.getElementById('transcribe-button'),
        processingNote: document.getElementById('processing-note'),
        transcriptContent: document.getElementById('transcript-content'),
        copyButton: document.getElementById('transcript-copy-button'),

        languageSelect: document.getElementById('language'),
        languageMenuButton: document.getElementById('language-menu-button'),
        currentLanguageLabel: document.getElementById('current-language-label'),

        themeToggle: document.getElementById('theme-toggle'),
        themeColor: document.querySelector('meta[name="theme-color"]'),

        settingsPage: document.getElementById('settings'),
        settingsInterfaceLanguage: document.getElementById('settings-interface-language'),
        settingsTheme: document.getElementById('settings-theme'),
        settingsDefaultLanguage: document.getElementById('settings-default-language'),
        settingsSaveStatus: document.getElementById('settings-save-status'),

        audioHeading: document.querySelector('.audio-panel .file-heading h3'),
        audioDescription: document.querySelector('.audio-panel .file-heading p'),
        audioNote: document.querySelector('.audio-panel .sample-note'),

        uploadTab: document.getElementById('tab-upload-file'),
        recordTab: document.getElementById('record'),

        recordingPanel: document.getElementById('recording-panel'),
        recordingIndicator: document.getElementById('recording-indicator'),
        recordingStatus: document.getElementById('recording-status'),
        recordingTimer: document.getElementById('recording-timer'),
        startRecordingButton: document.getElementById('start-recording-button'),
        stopRecordingButton: document.getElementById('stop-recording-button'),
        recordingPreview: document.getElementById('recording-preview'),
        useRecordingButton: document.getElementById('use-recording-button'),
        recordingError: document.getElementById('recording-error'),

        historyBody: document.getElementById('history-body'),
        fileSearch: document.getElementById('file-search'),

        navTranscribe: document.getElementById('nav-transcribe'),
        navRecord: document.getElementById('nav-record'),
        navHistory: document.getElementById('nav-history'),
        navSettings: document.getElementById('nav-settings'),

        heroBanner: document.querySelector('.hero-banner'),
        workspace: document.querySelector('.workspace'),
        resultsGrid: document.querySelector('.results-grid'),
        recentFiles: document.querySelector('.recent-files'),
        mainContent: document.querySelector('.main-content'),
        topbar: document.querySelector('.topbar')
    };

    const CONFIG = {
        uploadUrl: '/api/uploads',
        historyUrl: '/api/history',
        maxFileSize: 50 * 1024 * 1024,
        maxDurationSeconds: 5 * 60,
        recordingDurationSeconds: 5 * 60,
        allowedExtensions: ['mp3', 'wav', 'm4a', 'mp4', 'webm'],

        storageKeys: {
            interfaceLanguage: 'aureva-language',
            theme: 'aureva-theme',
            transcriptionLanguage: 'aureva-default-transcription-language'
        }
    };

    // 2. SAFE LOCAL STORAGE

    function readSetting(key, fallback) {
        try {
            return localStorage.getItem(key) ?? fallback;
        } catch (error) {
            console.warn('AUREVA: Local storage is unavailable.', error);
            return fallback;
        }
    }

    function saveSetting(key, value) {
        try {
            localStorage.setItem(key, value);
            return true;
        } catch (error) {
            console.warn('AUREVA: Could not save setting.', error);
            return false;
        }
    }

    // 3. APPLICATION STATE

    let selectedFile = null;
    let isTranscribing = false;

    let mediaRecorder = null;
    let recordingStream = null;
    let recordingChunks = [];
    let recordingInterval = null;
    let recordingSeconds = 0;
    let recordingBlob = null;
    let recordingObjectUrl = null;
    let fileSelectionVersion = 0;

    const savedInterfaceLanguage = readSetting(
        CONFIG.storageKeys.interfaceLanguage,
        'en'
    );

    let currentLanguage = ['en', 'ru'].includes(savedInterfaceLanguage)
        ? savedInterfaceLanguage
        : 'en';

    const savedTheme = readSetting(CONFIG.storageKeys.theme, 'dark');

    let currentTheme = ['dark', 'light'].includes(savedTheme)
        ? savedTheme
        : 'dark';

    const savedTranscriptionLanguage = readSetting(
        CONFIG.storageKeys.transcriptionLanguage,
        'en'
    );

    let defaultTranscriptionLanguage = ['en', 'ru'].includes(savedTranscriptionLanguage)
        ? savedTranscriptionLanguage
        : 'en';

    // 4. TRANSLATIONS

    const translations = {
        en: {
            // General interface
            sidebarLabel: 'Application sidebar',
            navigationLabel: 'Main navigation',
            brandSubtitle: 'Speech to text',
            navTranscribe: 'Transcribe',
            navRecord: 'Record',
            navHistory: 'History',
            navTemplates: 'Templates',
            navIntegrations: 'Integrations',
            navSettings: 'Settings',

            planLabel: 'FREE PLAN',
            freePlan: 'Free Plan',
            planUsage: '0 / 10 minutes used',
            upgrade: 'Upgrade',
            helpSupport: 'Help & Support',
            logout: 'Log out',

            searchPlaceholder: 'Search your files...',
            searchLabel: 'Search files',
            themeLabel: 'Theme',
            switchLanguage: 'Switch interface language',
            notifications: 'Notifications',
            myAccount: 'My Account',

            // Hero
            heroEyebrow: 'SPEECH TO TEXT',
            heroTitleFirst: 'Turn your voice',
            heroTitleSecond: 'into text',
            heroDescriptionFirst: 'Fast, clear transcription for your meetings,',
            heroDescriptionSecond: 'notes and ideas.',
            featureAccuracy: 'High accuracy',
            featureLanguages: '2 languages',
            featurePrivacy: 'Temporary files removed after processing',

            // Upload and recording
            uploadFile: 'Upload File',
            recordAudio: 'Record Audio',
            audioRecording: 'Audio recording',
            recordingDescription: 'Record your voice directly from your microphone. Maximum duration: 5 minutes.',
            startRecording: '● Start Recording',
            stopRecording: '■ Stop Recording',
            useRecording: 'Use Recording',

            selectAudioVideo: 'Select an audio or video file',
            dropzoneTitle: 'Drag & drop your audio or video file here',
            orText: 'or',
            browseFile: 'click to browse',
            supportedFormats: 'MP3, WAV, M4A, MP4, WEBM · Max 50 MB · Up to 5 minutes',

            transcriptionSettings: 'Transcription settings',
            language: 'Language',
            languageRussian: 'Russian',
            languageEnglish: 'English',
            transcribe: 'Transcribe',

            noAudioSelected: 'No audio selected',
            uploadToSeeDetails: 'Upload a file to see audio details.',
            audioPreviewNote: 'Audio preview will be available after transcription.',
            transcriptTitle: 'Transcript',
            transcriptPlaceholder: 'Your transcript will appear here.',
            copy: 'Copy',
            recentFiles: 'Recent Files',
            viewAll: 'View All',

            // History
            historyTitle: 'Transcription History',
            historyDescription: 'All your saved transcriptions in one place.',
            historyName: 'Name',
            historyDuration: 'Duration',
            historyLanguage: 'Language',
            historyCreated: 'Created',
            historyStatus: 'Status',
            completedStatus: 'Completed',
            backToTranscribe: 'Back to Transcribe',
            noHistory: 'No transcriptions yet.',
            historySearchPlaceholder: 'Search transcription history...',
            historySearchLabel: 'Search transcription history',
            selectAll: 'Select all',
            deleteSelected: 'Delete selected',
            deleteAll: 'Delete all',
            deleteRecord: 'Delete',
            confirmDeleteRecord: 'Delete this transcription?',
            confirmDeleteSelected: 'Delete selected transcriptions?',
            confirmDeleteAll: 'Delete all transcription history? This cannot be undone.',
            historyDeleted: 'History updated successfully.',
            deleteFailed: 'Could not delete transcription history.',
            nothingSelected: 'Select at least one transcription.',

            // Settings
            preferences: 'PREFERENCES',
            settingsTitle: 'Settings',
            settingsDescription: 'Manage your AUREVA application preferences.',
            general: 'General',
            interfaceLanguage: 'Interface Language',
            interfaceLanguageDescription: 'Choose the language used across the application.',
            appearance: 'Appearance',
            appearanceDescription: 'Choose your preferred application theme.',
            applicationTheme: 'Application theme',
            darkTheme: 'Dark',
            lightTheme: 'Light',

            transcriptionSettingsTitle: 'Transcription',
            defaultRecognitionLanguage: 'Default Recognition Language',
            defaultRecognitionDescription: 'Language selected automatically for new transcriptions.',
            defaultTranscriptionLanguage: 'Default transcription language',

            storagePrivacy: 'Storage & Privacy',
            uploadedFiles: 'Uploaded Files',
            uploadedFilesDescription: 'Temporary uploaded files are removed after processing.',
            automaticCleanup: 'Automatic cleanup',
            transcriptionHistory: 'Transcription History',
            transcriptionHistoryDescription: 'Your completed transcripts are stored in the application database.',
            databaseStorage: 'Database storage',

            about: 'About',
            aboutDescription: 'Speech-to-text application.',
            version: 'Version 0.1.0',

            footerTagline: 'Speech to text, made simple.',

            // Application messages
            noFile: 'No file selected',
            noAudio: 'No audio selected',
            uploadAudio: 'Upload a file to see audio details.',
            previewNote: 'Audio preview will be available after transcription.',
            selectFile: 'Select an audio or video file',
            invalidType: 'Unsupported file format. Use MP3, WAV, M4A, MP4 or WEBM.',
            fileTooLarge: 'File size exceeds the 50 MB limit.',
            durationTooLong: 'File duration exceeds the 5-minute limit.',
            durationUnavailable: 'Could not read media duration. The server will validate the file.',
            ready: 'Ready to transcribe',
            processing: 'Transcription in progress...',
            completed: 'Transcription completed successfully.',
            failed: 'Transcription failed.',
            noTranscript: 'The server returned an empty transcript.',
            copySuccess: 'Transcript copied.',
            copyFailed: 'Could not copy transcript.',
            noTranscriptToCopy: 'There is no transcript to copy.',
            recordingReady: 'Ready to record',
            recording: 'Recording...',
            recordingFinished: 'Recording completed',
            recordingUnsupported: 'Audio recording is not supported by this browser.',
            microphoneDenied: 'Microphone access was denied or is unavailable.',
            recordingEmpty: 'No audio was recorded.',
            recordingLimit: 'Maximum recording duration reached.',
            recordingError: 'Recording failed. Please try again.',
            noRecording: 'There is no recording to use.',
            searching: 'No matching files found.',
            selectFirst: 'Please select a file first.',
            requestFailed: 'Request failed. Please try again.',
            copying: 'Copying...',
            settingsSaved: 'Settings saved.',
            settingsSaveFailed: 'Could not save settings in this browser.',

            upgradeUnavailable: 'Upgrade functionality is not available yet.',
            noNotifications: 'No new notifications.',
            accountUnavailable: 'Account settings are not available yet.',
            authenticationUnavailable: 'Authentication is not available yet.',
            supportUnavailable: 'Support is not available yet.',

            switchToDarkTheme: 'Switch to dark theme',
            switchToLightTheme: 'Switch to light theme'
        },

        ru: {
            // General interface
            sidebarLabel: 'Боковая панель приложения',
            navigationLabel: 'Главная навигация',
            brandSubtitle: 'Преобразование речи в текст',
            navTranscribe: 'Транскрипция',
            navRecord: 'Запись',
            navHistory: 'История',
            navTemplates: 'Шаблоны',
            navIntegrations: 'Интеграции',
            navSettings: 'Настройки',

            planLabel: 'БЕСПЛАТНЫЙ ПЛАН',
            freePlan: 'Бесплатный план',
            planUsage: 'Использовано 0 / 10 минут',
            upgrade: 'Улучшить план',
            helpSupport: 'Помощь и поддержка',
            logout: 'Выйти',

            searchPlaceholder: 'Поиск файлов...',
            searchLabel: 'Поиск файлов',
            themeLabel: 'Тема оформления',
            switchLanguage: 'Сменить язык интерфейса',
            notifications: 'Уведомления',
            myAccount: 'Мой аккаунт',

            // Hero
            heroEyebrow: 'РЕЧЬ В ТЕКСТ',
            heroTitleFirst: 'Преобразуйте речь',
            heroTitleSecond: 'в текст',
            heroDescriptionFirst: 'Быстрая и точная транскрипция ваших встреч,',
            heroDescriptionSecond: 'заметок и идей.',
            featureAccuracy: 'Высокая точность',
            featureLanguages: '2 языка',
            featurePrivacy: 'Временные файлы удаляются после обработки',

            // Upload and recording
            uploadFile: 'Загрузить файл',
            recordAudio: 'Записать аудио',
            audioRecording: 'Запись аудио',
            recordingDescription: 'Записывайте голос напрямую через микрофон. Максимальная длительность: 5 минут.',
            startRecording: '● Начать запись',
            stopRecording: '■ Остановить запись',
            useRecording: 'Использовать запись',

            selectAudioVideo: 'Выберите аудио- или видеофайл',
            dropzoneTitle: 'Перетащите аудио- или видеофайл сюда',
            orText: 'или',
            browseFile: 'выберите файл',
            supportedFormats: 'MP3, WAV, M4A, MP4, WEBM · До 50 МБ · До 5 минут',

            transcriptionSettings: 'Настройки транскрипции',
            language: 'Язык',
            languageRussian: 'Русский',
            languageEnglish: 'Английский',
            transcribe: 'Транскрибировать',

            noAudioSelected: 'Аудио не выбрано',
            uploadToSeeDetails: 'Загрузите файл, чтобы увидеть информацию.',
            audioPreviewNote: 'Предпросмотр аудио будет доступен после транскрипции.',
            transcriptTitle: 'Результат транскрипции',
            transcriptPlaceholder: 'Здесь появится текст транскрипции.',
            copy: 'Копировать',
            recentFiles: 'Последние файлы',
            viewAll: 'Посмотреть все',

            // History
            historyTitle: 'История транскрипций',
            historyDescription: 'Все сохранённые транскрипции в одном месте.',
            historyName: 'Название',
            historyDuration: 'Длительность',
            historyLanguage: 'Язык',
            historyCreated: 'Дата создания',
            historyStatus: 'Статус',
            completedStatus: 'Завершено',
            backToTranscribe: 'Вернуться к транскрипции',
            noHistory: 'Транскрипций пока нет.',
            historySearchPlaceholder: 'Поиск в истории транскрипций...',
            historySearchLabel: 'Поиск в истории транскрипций',
            selectAll: 'Выбрать все',
            deleteSelected: 'Удалить выбранные',
            deleteAll: 'Удалить всё',
            deleteRecord: 'Удалить',
            confirmDeleteRecord: 'Удалить эту транскрипцию?',
            confirmDeleteSelected: 'Удалить выбранные транскрипции?',
            confirmDeleteAll: 'Удалить всю историю транскрипций? Это действие нельзя отменить.',
            historyDeleted: 'История успешно обновлена.',
            deleteFailed: 'Не удалось удалить историю транскрипций.',
            nothingSelected: 'Сначала выберите хотя бы одну транскрипцию.',

            // Settings
            preferences: 'ПАРАМЕТРЫ',
            settingsTitle: 'Настройки',
            settingsDescription: 'Управление настройками приложения AUREVA.',
            general: 'Общие',
            interfaceLanguage: 'Язык интерфейса',
            interfaceLanguageDescription: 'Выберите язык отображения приложения.',
            appearance: 'Внешний вид',
            appearanceDescription: 'Выберите тему оформления приложения.',
            applicationTheme: 'Тема приложения',
            darkTheme: 'Тёмная',
            lightTheme: 'Светлая',

            transcriptionSettingsTitle: 'Транскрипция',
            defaultRecognitionLanguage: 'Язык распознавания по умолчанию',
            defaultRecognitionDescription: 'Язык, который будет автоматически выбран для новых транскрипций.',
            defaultTranscriptionLanguage: 'Язык транскрипции по умолчанию',

            storagePrivacy: 'Хранение и конфиденциальность',
            uploadedFiles: 'Загруженные файлы',
            uploadedFilesDescription: 'Временные загруженные файлы удаляются после обработки.',
            automaticCleanup: 'Автоматическая очистка',
            transcriptionHistory: 'История транскрипций',
            transcriptionHistoryDescription: 'Завершённые транскрипции сохраняются в базе данных приложения.',
            databaseStorage: 'Хранение в базе данных',

            about: 'О приложении',
            aboutDescription: 'Приложение для преобразования речи в текст.',
            version: 'Версия 0.1.0',

            footerTagline: 'Преобразование речи в текст — просто.',

            // Application messages
            noFile: 'Файл не выбран',
            noAudio: 'Аудио не выбрано',
            uploadAudio: 'Загрузите файл, чтобы увидеть информацию.',
            previewNote: 'Предпросмотр аудио будет доступен после транскрипции.',
            selectFile: 'Выберите аудио- или видеофайл',
            invalidType: 'Неподдерживаемый формат. Используйте MP3, WAV, M4A, MP4 или WEBM.',
            fileTooLarge: 'Размер файла превышает ограничение 50 МБ.',
            durationTooLong: 'Длительность файла превышает ограничение в 5 минут.',
            durationUnavailable: 'Не удалось определить длительность. Сервер проверит файл.',
            ready: 'Готово к транскрипции',
            processing: 'Выполняется транскрипция...',
            completed: 'Транскрипция успешно завершена.',
            failed: 'Ошибка транскрипции.',
            noTranscript: 'Сервер вернул пустой текст.',
            copySuccess: 'Текст скопирован.',
            copyFailed: 'Не удалось скопировать текст.',
            noTranscriptToCopy: 'Нет текста для копирования.',
            recordingReady: 'Готово к записи',
            recording: 'Идёт запись...',
            recordingFinished: 'Запись завершена',
            recordingUnsupported: 'Этот браузер не поддерживает запись аудио.',
            microphoneDenied: 'Нет доступа к микрофону.',
            recordingEmpty: 'Запись не содержит аудио.',
            recordingLimit: 'Достигнута максимальная длительность записи.',
            recordingError: 'Ошибка записи. Попробуйте ещё раз.',
            noRecording: 'Нет записи для использования.',
            searching: 'Совпадений не найдено.',
            selectFirst: 'Сначала выберите файл.',
            requestFailed: 'Ошибка запроса. Попробуйте ещё раз.',
            copying: 'Копирование...',
            settingsSaved: 'Настройки сохранены.',
            settingsSaveFailed: 'Не удалось сохранить настройки в браузере.',

            upgradeUnavailable: 'Функция улучшения тарифа пока недоступна.',
            noNotifications: 'Новых уведомлений нет.',
            accountUnavailable: 'Настройки аккаунта пока недоступны.',
            authenticationUnavailable: 'Авторизация пока недоступна.',
            supportUnavailable: 'Служба поддержки пока недоступна.',

            switchToDarkTheme: 'Переключить на тёмную тему',
            switchToLightTheme: 'Переключить на светлую тему'
        }
    };

    function t(key) {
        return translations[currentLanguage]?.[key]
            || translations.en[key]
            || key;
    }

    // 5. GENERAL TRANSLATION

    function translateStaticInterface() {
        document.documentElement.lang = currentLanguage;

        document.querySelectorAll('[data-i18n]').forEach(element => {
            element.textContent = t(element.dataset.i18n);
        });

        document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
            element.setAttribute(
                'placeholder',
                t(element.dataset.i18nPlaceholder)
            );
        });

        document.querySelectorAll('[data-i18n-aria-label]').forEach(element => {
            element.setAttribute(
                'aria-label',
                t(element.dataset.i18nAriaLabel)
            );
        });

        const textPairs = [
            ['Speech to text', 'Преобразование речи в текст'],
            ['Search your files ...', 'Поиск файлов ...'],
            ['Search your files...', 'Поиск файлов...'],
            ['My Account', 'Мой аккаунт'],

            ['Transcribe', 'Транскрипция'],
            ['Record', 'Запись'],
            ['History', 'История'],
            ['Templates', 'Шаблоны'],
            ['Integrations', 'Интеграции'],
            ['Settings', 'Настройки'],

            ['FREE PLAN', 'БЕСПЛАТНЫЙ ПЛАН'],
            ['Free Plan', 'Бесплатный план'],
            ['0 / 10 minutes used', 'Использовано 0 / 10 минут'],
            ['Upgrade', 'Улучшить план'],
            ['Help & Support', 'Помощь и поддержка'],
            ['Log out', 'Выйти'],

            ['SPEECH TO TEXT', 'РЕЧЬ В ТЕКСТ'],
            ['Turn your voice', 'Преобразуйте речь'],
            ['into text', 'в текст'],
            [
                'Fast, clear transcription for your meetings,',
                'Быстрая и точная транскрипция ваших встреч,'
            ],
            ['notes and ideas.', 'заметок и идей.'],
            ['High accuracy', 'Высокая точность'],
            ['2 languages', '2 языка'],
            [
                'Temporary files removed after processing',
                'Временные файлы удаляются после обработки'
            ],

            ['Upload File', 'Загрузить файл'],
            ['Record Audio', 'Записать аудио'],
            ['Audio recording', 'Запись аудио'],
            [
                'Record your voice directly from your microphone. Maximum duration: 5 minutes.',
                'Записывайте голос через микрофон. Максимальная длительность: 5 минут.'
            ],

            ['Select an audio or video file', 'Выберите аудио- или видеофайл'],
            [
                'Drag & drop your audio or video file here',
                'Перетащите аудио- или видеофайл сюда'
            ],
            ['or', 'или'],
            ['click to browse', 'выберите файл'],

            [
                'MP3, WAV, M4A, MP4, WEBM · Max 50 MB · Up to 5 minutes',
                'MP3, WAV, M4A, MP4, WEBM · До 50 МБ · До 5 минут'
            ],
            [
                'MP3, WAV, M4A, MP4, WEBM - Max 50 MB - Up to 5 minutes',
                'MP3, WAV, M4A, MP4, WEBM - До 50 МБ - До 5 минут'
            ],

            ['Transcription settings', 'Настройки транскрипции'],
            ['Language', 'Язык'],
            ['English', 'Английский'],
            ['Russian', 'Русский'],

            ['Transcript', 'Текст транскрипции'],
            ['Copy', 'Копировать'],
            ['Your transcript will appear here.', 'Здесь появится текст транскрипции.'],

            ['Recent Files', 'Последние файлы'],
            ['View All', 'Посмотреть все'],
            ['Name', 'Название'],
            ['Duration', 'Длительность'],
            ['Created', 'Дата создания'],
            ['Status', 'Статус'],
            ['Completed', 'Завершено'],

            ['PREFERENCES', 'ПАРАМЕТРЫ'],
            ['General', 'Общие'],
            ['Interface Language', 'Язык интерфейса'],
            [
                'Choose the language used across the application.',
                'Выберите язык отображения приложения.'
            ],
            ['Appearance', 'Внешний вид'],
            [
                'Choose your preferred application theme.',
                'Выберите тему оформления приложения.'
            ],
            ['Application theme', 'Тема приложения'],
            ['Dark', 'Тёмная'],
            ['Light', 'Светлая'],

            ['Default Recognition Language', 'Язык распознавания по умолчанию'],
            [
                'Language selected automatically for new transcriptions.',
                'Язык, автоматически выбираемый для новых транскрипций.'
            ],
            ['Default transcription language', 'Язык транскрипции по умолчанию'],

            ['Storage & Privacy', 'Хранение и конфиденциальность'],
            ['Uploaded Files', 'Загруженные файлы'],
            [
                'Temporary uploaded files are removed after processing.',
                'Временные загруженные файлы удаляются после обработки.'
            ],
            ['Automatic cleanup', 'Автоматическая очистка'],
            ['Transcription History', 'История транскрипций'],
            [
                'Your completed transcripts are stored in the application database.',
                'Завершённые транскрипции сохраняются в базе данных приложения.'
            ],
            ['Database storage', 'Хранение в базе данных'],

            ['About', 'О приложении'],
            ['Speech-to-text application.', 'Приложение для преобразования речи в текст.'],
            ['Version 0.1.0', 'Версия 0.1.0'],
            ['Speech to text, made simple.', 'Преобразование речи в текст — просто.']
        ];

        const textMap = new Map();

        textPairs.forEach(([english, russian]) => {
            const source = currentLanguage === 'ru' ? english : russian;
            const target = currentLanguage === 'ru' ? russian : english;

            textMap.set(source, target);
        });

        const walker = document.createTreeWalker(
            document.body,
            NodeFilter.SHOW_TEXT
        );

        let node;

        while ((node = walker.nextNode())) {
            const originalText = node.nodeValue;
            const normalizedText = originalText.trim().replace(/\s+/g, ' ');

            if (!textMap.has(normalizedText)) continue;

            const leadingWhitespace = originalText.match(/^\s*/)[0];
            const trailingWhitespace = originalText.match(/\s*$/)[0];

            node.nodeValue =
                leadingWhitespace +
                textMap.get(normalizedText) +
                trailingWhitespace;
        }

        const attributePairs = [
            ['Search your files ...', 'Поиск файлов ...'],
            ['Search your files...', 'Поиск файлов...'],
            ['Search files', 'Поиск файлов'],
            ['My Account', 'Мой аккаунт'],
            ['Notifications', 'Уведомления'],
            ['Switch interface language', 'Сменить язык интерфейса']
        ];

        const attributeMap = new Map();

        attributePairs.forEach(([english, russian]) => {
            const source = currentLanguage === 'ru' ? english : russian;
            const target = currentLanguage === 'ru' ? russian : english;

            attributeMap.set(source, target);
        });

        document.querySelectorAll('[placeholder], [aria-label], [title]').forEach(element => {
            ['placeholder', 'aria-label', 'title'].forEach(attribute => {
                const value = element.getAttribute(attribute);

                if (value && attributeMap.has(value)) {
                    element.setAttribute(attribute, attributeMap.get(value));
                }
            });
        });
    }

    // 6. HELPERS

    function formatFileSize(bytes) {
        if (bytes < 1024) return `${bytes} B`;
        if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
        return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
    }

    function formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;

        return `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
    }

    function showProcessingMessage(message, isError = false) {
        if (!elements.processingNote) return;

        elements.processingNote.textContent = message;
        elements.processingNote.style.color = isError ? '#ff5b5b' : '';
    }

    function showRecordingError(message) {
        if (elements.recordingError) {
            elements.recordingError.textContent = message;
        }
    }

    function clearRecordingError() {
        showRecordingError('');
    }

    function setTranscribeButtonState(disabled, text = null) {
        if (!elements.transcribeButton) return;

        elements.transcribeButton.disabled = disabled;

        const label = elements.transcribeButton.querySelector('[data-i18n="transcribe"]');

        if (label) {
            label.textContent = text || t('transcribe');
        } else {
            elements.transcribeButton.textContent = text || t('transcribe');
        }
    }

    function showSettingsSaveStatus(success) {
        if (!elements.settingsSaveStatus) return;

        elements.settingsSaveStatus.textContent = success
            ? t('settingsSaved')
            : t('settingsSaveFailed');

        elements.settingsSaveStatus.style.color = success ? '' : '#ff5b5b';
    }

    // 7. TRANSCRIPT

    function getTranscriptText() {
        if (!elements.transcriptContent) return '';

        const paragraphs = elements.transcriptContent.querySelectorAll(
            'p:not(.transcript-placeholder)'
        );

        return Array.from(paragraphs)
            .map(paragraph => paragraph.textContent.trim())
            .filter(Boolean)
            .join('\n');
    }

    function displayTranscript(transcript) {
        if (!elements.transcriptContent) return;

        elements.transcriptContent.replaceChildren();

        const paragraph = document.createElement('p');
        paragraph.textContent = transcript;

        elements.transcriptContent.appendChild(paragraph);
    }

    function resetTranscript() {
        if (elements.transcriptContent) {
            elements.transcriptContent.replaceChildren();

            const placeholder = document.createElement('p');
            placeholder.className = 'transcript-placeholder';
            placeholder.textContent = t('transcriptPlaceholder');

            elements.transcriptContent.appendChild(placeholder);
        }

        if (elements.audioHeading) elements.audioHeading.textContent = t('noAudio');
        if (elements.audioDescription) elements.audioDescription.textContent = t('uploadAudio');
        if (elements.audioNote) elements.audioNote.textContent = t('previewNote');

        showProcessingMessage(t('ready'));
    }

    // 8. FILE VALIDATION

    function validateFileType(file) {
        const extension = file.name.split('.').pop().toLowerCase();
        return CONFIG.allowedExtensions.includes(extension);
    }

    function validateFileSize(file) {
        return file.size <= CONFIG.maxFileSize;
    }

    function readMediaDuration(file) {
        return new Promise(resolve => {
            const objectUrl = URL.createObjectURL(file);

            const isVideo = file.type.startsWith('video/')
                || ['mp4', 'webm'].includes(file.name.split('.').pop().toLowerCase());

            const media = document.createElement(isVideo ? 'video' : 'audio');

            media.preload = 'metadata';

            media.onloadedmetadata = () => {
                const duration = media.duration;
                URL.revokeObjectURL(objectUrl);
                media.remove();
                resolve(Number.isFinite(duration) ? duration : null);
            };

            media.onerror = () => {
                URL.revokeObjectURL(objectUrl);
                media.remove();
                resolve(null);
            };

            media.src = objectUrl;
        });
    }

    async function validateFile(file) {
        if (!file) return {valid: false, message: t('selectFirst')};
        if (file.size === 0) {
            return {valid: false, message: 'File is empty.'};
        }
        if (!validateFileType(file)) return {valid: false, message: t('invalidType')};
        if (!validateFileSize(file)) return {valid: false, message: t('fileTooLarge')};

        const duration = await readMediaDuration(file);

        if (duration !== null && duration > CONFIG.maxDurationSeconds) {
            return {valid: false, message: t('durationTooLong')};
        }

        return {valid: true, duration};
    }

    // 9. FILE SELECTION

    function updateAudioInformation(file) {
        if (elements.audioHeading) elements.audioHeading.textContent = file.name;
        if (elements.audioDescription) elements.audioDescription.textContent = formatFileSize(file.size);
        if (elements.audioNote) elements.audioNote.textContent = t('previewNote');
    }

    async function handleFileSelection(file) {
        const selectionVersion = ++fileSelectionVersion;

        selectedFile = null;

        if (elements.fileInput) elements.fileInput.value = '';

        setTranscribeButtonState(true);

        if (!file) {
            if (elements.selectedFileName) elements.selectedFileName.textContent = t('noFile');
            resetTranscript();
            return;
        }

        if (elements.selectedFileName) elements.selectedFileName.textContent = file.name;

        showProcessingMessage(t('ready'));

        const validation = await validateFile(file);

        if (selectionVersion !== fileSelectionVersion) return;

        if (!validation.valid) {
            if (elements.selectedFileName) {
                elements.selectedFileName.textContent = t('noFile');
            }
            resetTranscript();
            showProcessingMessage(validation.message, true);
            return;
        }

        selectedFile = file;

        updateAudioInformation(file);
        resetTranscript();

        if (validation.duration === null) {
            showProcessingMessage(t('durationUnavailable'));
        }

        setTranscribeButtonState(false);
    }

    elements.fileInput?.addEventListener('change', event => {
        handleFileSelection(event.target.files?.[0]);
    });

    // 10. DRAG AND DROP

    if (elements.dropzone) {
        elements.dropzone.addEventListener('click', event => {
            if (event.target.closest('label') || event.target === elements.fileInput) return;
            elements.fileInput?.click();
        });

        elements.dropzone.addEventListener('keydown', event => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                elements.fileInput?.click();
            }
        });

        elements.dropzone.addEventListener('dragover', event => {
            event.preventDefault();
            elements.dropzone.classList.add('is-dragging');
        });

        elements.dropzone.addEventListener('dragleave', event => {
            if (!elements.dropzone.contains(event.relatedTarget)) {
                elements.dropzone.classList.remove('is-dragging');
            }
        });

        elements.dropzone.addEventListener('drop', event => {
            event.preventDefault();
            elements.dropzone.classList.remove('is-dragging');
            handleFileSelection(event.dataTransfer?.files?.[0]);
        });
    }

    // 11. TRANSCRIPTION API

    async function transcribeFile() {
        if (isTranscribing) return;

        if (!selectedFile) {
            showProcessingMessage(t('selectFirst'), true);
            return;
        }

        isTranscribing = true;

        setTranscribeButtonState(true, t('processing'));
        showProcessingMessage(t('processing'));

        try {
            const formData = new FormData();

            formData.append('file', selectedFile);
            formData.append(
                'language',
                elements.languageSelect?.value || defaultTranscriptionLanguage
            );

            const response = await fetch(CONFIG.uploadUrl, {
                method: 'POST',
                body: formData
            });

            const responseText = await response.text();

            let result = {};

            try {
                result = responseText ? JSON.parse(responseText) : {};
            } catch {
                throw new Error(responseText || `HTTP ${response.status}`);
            }

            if (!response.ok) {
                throw new Error(result.message || result.error || `HTTP ${response.status}`);
            }

            const transcript = result.transcript;

            if (typeof transcript !== 'string' || transcript.trim().length === 0) {
                throw new Error(t('noTranscript'));
            }

            displayTranscript(transcript);
            updateAudioInformation(selectedFile);
            showProcessingMessage(t('completed'));

            await loadHistory();

        } catch (error) {
            console.error('AUREVA transcription error:', error);
            showProcessingMessage(error.message || t('failed'), true);

        } finally {
            isTranscribing = false;
            setTranscribeButtonState(!selectedFile);
        }
    }

    elements.transcribeButton?.addEventListener('click', transcribeFile);

    // 12. COPY TRANSCRIPT

    async function copyTranscript() {
        const transcript = getTranscriptText();

        if (!transcript) {
            showProcessingMessage(t('noTranscriptToCopy'), true);
            return;
        }

        try {
            await navigator.clipboard.writeText(transcript);
            showProcessingMessage(t('copySuccess'));

        } catch (error) {
            console.error('Clipboard error:', error);

            const textarea = document.createElement('textarea');
            textarea.value = transcript;
            textarea.style.position = 'fixed';
            textarea.style.opacity = '0';

            document.body.appendChild(textarea);
            textarea.select();

            const copied = document.execCommand('copy');
            textarea.remove();

            showProcessingMessage(copied ? t('copySuccess') : t('copyFailed'), !copied);
        }
    }

    elements.copyButton?.addEventListener('click', copyTranscript);

    // 13. AUDIO RECORDING

    function getSupportedRecordingMimeType() {
        if (!window.MediaRecorder) return null;

        const mimeTypes = [
            'audio/webm;codecs=opus',
            'audio/webm',
            'audio/mp4'
        ];

        return mimeTypes.find(type => MediaRecorder.isTypeSupported(type)) || '';
    }

    function updateRecordingTimer() {
        if (elements.recordingTimer) {
            elements.recordingTimer.textContent = formatTime(recordingSeconds);
        }
    }

    function releaseRecordingStream() {
        if (recordingStream) {
            recordingStream.getTracks().forEach(track => track.stop());
            recordingStream = null;
        }
    }

    function clearRecordingObjectUrl() {
        if (recordingObjectUrl) {
            URL.revokeObjectURL(recordingObjectUrl);
            recordingObjectUrl = null;
        }

        if (elements.recordingPreview) {
            elements.recordingPreview.removeAttribute('src');
            elements.recordingPreview.load();
        }
    }

    function resetRecordingState() {
        if (recordingInterval) {
            clearInterval(recordingInterval);
            recordingInterval = null;
        }

        releaseRecordingStream();

        recordingChunks = [];
        recordingSeconds = 0;

        updateRecordingTimer();

        elements.recordingIndicator?.classList.remove('is-recording');

        if (elements.startRecordingButton) {
            elements.startRecordingButton.disabled = false;
            elements.startRecordingButton.textContent = t('startRecording');
        }

        if (elements.stopRecordingButton) {
            elements.stopRecordingButton.disabled = true;
        }

        if (elements.recordingStatus) {
            elements.recordingStatus.textContent = t('recordingReady');
        }
    }

    async function startRecording() {
        clearRecordingError();

        if (!navigator.mediaDevices?.getUserMedia || !window.MediaRecorder) {
            showRecordingError(t('recordingUnsupported'));
            return;
        }

        try {
            clearRecordingObjectUrl();

            recordingBlob = null;
            recordingChunks = [];
            recordingSeconds = 0;

            updateRecordingTimer();

            recordingStream = await navigator.mediaDevices.getUserMedia({ audio: true });

            const mimeType = getSupportedRecordingMimeType();

            mediaRecorder = mimeType
                ? new MediaRecorder(recordingStream, { mimeType })
                : new MediaRecorder(recordingStream);

            mediaRecorder.addEventListener('dataavailable', event => {
                if (event.data && event.data.size > 0) {
                    recordingChunks.push(event.data);
                }
            });

            mediaRecorder.addEventListener('stop', () => {
                const type = mediaRecorder.mimeType || 'audio/webm';

                recordingBlob = new Blob(recordingChunks, { type });

                if (recordingBlob.size === 0) {
                    showRecordingError(t('recordingEmpty'));

                    if (elements.useRecordingButton) {
                        elements.useRecordingButton.disabled = true;
                    }

                    resetRecordingState();
                    return;
                }

                clearRecordingObjectUrl();

                recordingObjectUrl = URL.createObjectURL(recordingBlob);

                if (elements.recordingPreview) {
                    elements.recordingPreview.src = recordingObjectUrl;
                    elements.recordingPreview.hidden = false;
                }

                if (elements.useRecordingButton) {
                    elements.useRecordingButton.disabled = false;
                }

                if (elements.recordingStatus) {
                    elements.recordingStatus.textContent = t('recordingFinished');
                }

                resetRecordingState();
            });

            mediaRecorder.start();

            if (elements.startRecordingButton) elements.startRecordingButton.disabled = true;
            if (elements.stopRecordingButton) elements.stopRecordingButton.disabled = false;

            if (elements.recordingStatus) {
                elements.recordingStatus.textContent = t('recording');
            }

            elements.recordingIndicator?.classList.add('is-recording');

            recordingInterval = setInterval(() => {
                recordingSeconds++;
                updateRecordingTimer();

                if (recordingSeconds >= CONFIG.recordingDurationSeconds) {
                    showRecordingError(t('recordingLimit'));
                    stopRecording();
                }
            }, 1000);

        } catch (error) {
            console.error('AUREVA recording error:', error);

            releaseRecordingStream();
            showRecordingError(t('microphoneDenied'));
            resetRecordingState();
        }
    }

    function stopRecording() {
        if (mediaRecorder && mediaRecorder.state !== 'inactive') {
            mediaRecorder.stop();
        }

        if (recordingInterval) {
            clearInterval(recordingInterval);
            recordingInterval = null;
        }

        releaseRecordingStream();
        elements.recordingIndicator?.classList.remove('is-recording');

        if (elements.startRecordingButton) elements.startRecordingButton.disabled = false;
        if (elements.stopRecordingButton) elements.stopRecordingButton.disabled = true;
    }

    async function useRecording() {
        if (!recordingBlob || recordingBlob.size === 0) {
            showRecordingError(t('noRecording'));
            return;
        }

        const extension = recordingBlob.type.includes('mp4') ? 'mp4' : 'webm';

        const file = new File(
            [recordingBlob],
            `aureva-recording-${Date.now()}.${extension}`,
            {
                type: recordingBlob.type,
                lastModified: Date.now()
            }
        );

        if (elements.recordingPreview) elements.recordingPreview.hidden = true;

        showUploadTab();
        await handleFileSelection(file);
    }

    elements.startRecordingButton?.addEventListener('click', startRecording);
    elements.stopRecordingButton?.addEventListener('click', stopRecording);
    elements.useRecordingButton?.addEventListener('click', useRecording);

    // 14. UPLOAD / RECORD TABS

    function showUploadTab() {
        elements.uploadTab?.classList.add('active');
        elements.recordTab?.classList.remove('active');

        if (elements.recordingPanel) elements.recordingPanel.hidden = true;
        if (elements.dropzone) elements.dropzone.hidden = false;
    }

    function showRecordingTab() {
        elements.recordTab?.classList.add('active');
        elements.uploadTab?.classList.remove('active');

        if (elements.recordingPanel) elements.recordingPanel.hidden = false;
        if (elements.dropzone) elements.dropzone.hidden = true;
    }

    elements.uploadTab?.addEventListener('click', showUploadTab);
    elements.recordTab?.addEventListener('click', showRecordingTab);

    // 15. LANGUAGE SWITCHING

    function applyLanguage(language) {
        if (!['en', 'ru'].includes(language)) language = 'en';

        currentLanguage = language;

        saveSetting(CONFIG.storageKeys.interfaceLanguage, language);

        translateStaticInterface();

        if (elements.currentLanguageLabel) {
            elements.currentLanguageLabel.textContent = language.toUpperCase();
        }

        if (elements.settingsInterfaceLanguage) {
            elements.settingsInterfaceLanguage.value = language;
        }

        if (elements.selectedFileName && !selectedFile) {
            elements.selectedFileName.textContent = t('noFile');
        }

        if (elements.audioHeading && !selectedFile) {
            elements.audioHeading.textContent = t('noAudio');
        }

        if (elements.audioDescription && !selectedFile) {
            elements.audioDescription.textContent = t('uploadAudio');
        }

        if (elements.audioNote && !selectedFile) {
            elements.audioNote.textContent = t('previewNote');
        }

        if (elements.transcribeButton && !isTranscribing) {
            setTranscribeButtonState(!selectedFile);
        }

        if (elements.copyButton) {
            elements.copyButton.textContent = t('copy');
        }

        if (elements.recordingStatus && !mediaRecorder) {
            elements.recordingStatus.textContent = t('recordingReady');
        }

        if (elements.startRecordingButton && !mediaRecorder) {
            elements.startRecordingButton.textContent = t('startRecording');
        }

        if (elements.stopRecordingButton) {
            elements.stopRecordingButton.textContent = t('stopRecording');
        }

        if (elements.useRecordingButton) {
            elements.useRecordingButton.textContent = t('useRecording');
        }

        if (!selectedFile && !isTranscribing) {
            showProcessingMessage(t('ready'));
        }

        if (historySearchInput) {
            historySearchInput.placeholder = t('historySearchPlaceholder');
            historySearchInput.setAttribute('aria-label', t('historySearchLabel'));
        }

        updateHistoryPageLanguage();
    }

    elements.languageSelect?.addEventListener('change', event => {
        const language = event.target.value;

        if (!['en', 'ru'].includes(language)) {
            event.target.value = defaultTranscriptionLanguage;
            return;
        }

        defaultTranscriptionLanguage = language;

        const saved = saveSetting(
            CONFIG.storageKeys.transcriptionLanguage,
            language
        );

        if (elements.settingsDefaultLanguage) {
            elements.settingsDefaultLanguage.value = language;
        }

        showSettingsSaveStatus(saved);
    });

    elements.languageMenuButton?.addEventListener('click', () => {
        applyLanguage(currentLanguage === 'en' ? 'ru' : 'en');
    });

    // 16. THEME SWITCHING

    function applyTheme(theme) {
        if (!['dark', 'light'].includes(theme)) {
            theme = 'dark';
        }

        currentTheme = theme;

        const isLight = theme === 'light';

        document.body.classList.toggle('light-theme', isLight);
        document.documentElement.dataset.theme = theme;

        if (elements.themeColor) {
            elements.themeColor.content = isLight ? '#ffffff' : '#0b0b0b';
        }

        if (elements.themeToggle) {
            elements.themeToggle.setAttribute(
                'aria-label',
                isLight ? t('switchToDarkTheme') : t('switchToLightTheme')
            );
        }

        if (elements.settingsTheme) {
            elements.settingsTheme.value = theme;
        }

        saveSetting(CONFIG.storageKeys.theme, theme);
    }

    elements.themeToggle?.addEventListener('click', () => {
        applyTheme(currentTheme === 'dark' ? 'light' : 'dark');
    });

    // 17. SETTINGS

    function initializeSettings() {
        if (elements.settingsInterfaceLanguage) {
            elements.settingsInterfaceLanguage.value = currentLanguage;
        }

        if (elements.settingsTheme) {
            elements.settingsTheme.value = currentTheme;
        }

        if (elements.settingsDefaultLanguage) {
            elements.settingsDefaultLanguage.value = defaultTranscriptionLanguage;
        }

        if (elements.languageSelect) {
            elements.languageSelect.value = defaultTranscriptionLanguage;
        }
    }

    elements.settingsInterfaceLanguage?.addEventListener('change', event => {
        const language = event.target.value;

        if (!['en', 'ru'].includes(language)) {
            event.target.value = currentLanguage;
            return;
        }

        applyLanguage(language);

        showSettingsSaveStatus(
            readSetting(CONFIG.storageKeys.interfaceLanguage, null) === language
        );
    });

    elements.settingsTheme?.addEventListener('change', event => {
        const theme = event.target.value;

        if (!['dark', 'light'].includes(theme)) {
            event.target.value = currentTheme;
            return;
        }

        applyTheme(theme);

        showSettingsSaveStatus(
            readSetting(CONFIG.storageKeys.theme, null) === theme
        );
    });

    elements.settingsDefaultLanguage?.addEventListener('change', event => {
        const language = event.target.value;

        if (!['en', 'ru'].includes(language)) {
            event.target.value = defaultTranscriptionLanguage;
            return;
        }

        defaultTranscriptionLanguage = language;

        if (elements.languageSelect) {
            elements.languageSelect.value = language;
        }

        const saved = saveSetting(
            CONFIG.storageKeys.transcriptionLanguage,
            language
        );

        showSettingsSaveStatus(saved);
    });

    // 18. HISTORY VIEW

    let historyPage = null;
    let historyPageBody = null;
    let historySearchInput = null;
    let historyStatus = null;
    let selectAllHistoryCheckbox = null;
    let deleteSelectedButton = null;
    let deleteAllButton = null;
    let cachedHistory = [];
    const selectedHistoryIds = new Set();
    let isDeletingHistory = false;

    function createHistoryPage() {
        if (!elements.mainContent || !elements.topbar) return;

        historyPage = document.createElement('section');
        historyPage.id = 'history-page';
        historyPage.className = 'panel history-page';
        historyPage.hidden = true;

        historyPage.innerHTML = `
            <div class="panel-heading">
                <div>
                    <h1 class="history-page-title"></h1>
                    <p class="history-page-description"></p>
                </div>
                <button type="button"
                        class="secondary-button"
                        id="history-back-button"></button>
            </div>

            <div class="history-page-search">
                <input id="history-page-search"
                       type="search"
                       placeholder=""
                       aria-label="">
            </div>

            <div class="history-actions">
                <label class="history-select-all">
                    <input id="history-select-all" type="checkbox">
                    <span></span>
                </label>
                <div class="history-action-buttons">
                    <button type="button"
                            class="secondary-button"
                            id="delete-selected-button"
                            disabled></button>
                    <button type="button"
                            class="danger-button"
                            id="delete-all-button"
                            disabled></button>
                </div>
            </div>

            <p id="history-action-status" role="status" aria-live="polite"></p>

            <div class="table-scroll">
                <table>
                    <thead>
                        <tr>
                            <th></th>
                            <th></th>
                            <th></th>
                            <th></th>
                            <th></th>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody id="history-page-body"></tbody>
                </table>
            </div>
        `;

        elements.topbar.insertAdjacentElement('afterend', historyPage);

        historyPageBody = historyPage.querySelector('#history-page-body');
        historySearchInput = historyPage.querySelector('#history-page-search');
        historyStatus = historyPage.querySelector('#history-action-status');
        selectAllHistoryCheckbox = historyPage.querySelector('#history-select-all');
        deleteSelectedButton = historyPage.querySelector('#delete-selected-button');
        deleteAllButton = historyPage.querySelector('#delete-all-button');

        selectAllHistoryCheckbox.addEventListener('change', () => {
            getFilteredHistory().forEach(item => {
                if (item.id === null || item.id === undefined) return;

                if (selectAllHistoryCheckbox.checked) {
                    selectedHistoryIds.add(String(item.id));
                } else {
                    selectedHistoryIds.delete(String(item.id));
                }
            });

            renderHistoryPageTable();
        });

        deleteSelectedButton.addEventListener('click', deleteSelectedHistory);
        deleteAllButton.addEventListener('click', deleteAllHistory);

        historyPageBody.addEventListener('change', event => {
            const checkbox = event.target.closest('.history-record-checkbox');
            if (!checkbox) return;

            const id = checkbox.dataset.id;
            if (checkbox.checked) {
                selectedHistoryIds.add(id);
            } else {
                selectedHistoryIds.delete(id);
            }

            updateHistorySelection();
        });

        historyPageBody.addEventListener('click', event => {
            const button = event.target.closest('.history-delete-button');
            if (!button) return;

            deleteHistoryRecord(button.dataset.id);
        });

        historyPage.querySelector('#history-back-button')
            .addEventListener('click', () => showPage('transcribe'));

        historySearchInput.addEventListener('input', renderHistoryPageTable);

        updateHistoryPageLanguage();
    }

    function updateHistoryPageLanguage() {
        if (!historyPage) return;

        const title = historyPage.querySelector('.history-page-title');
        const description = historyPage.querySelector('.history-page-description');
        const backButton = historyPage.querySelector('#history-back-button');
        const headers = historyPage.querySelectorAll('thead th');

        if (title) title.textContent = t('historyTitle');
        if (description) description.textContent = t('historyDescription');
        if (backButton) backButton.textContent = t('backToTranscribe');

        if (historySearchInput) {
            historySearchInput.placeholder = t('historySearchPlaceholder');
            historySearchInput.setAttribute('aria-label', t('historySearchLabel'));
        }

        const headerKeys = [
            '',
            'historyName',
            'historyDuration',
            'historyLanguage',
            'historyCreated',
            'historyStatus',
            ''
        ];

        headers.forEach((header, index) => {
            header.textContent = headerKeys[index] ? t(headerKeys[index]) : '';
        });

        const selectAllLabel = historyPage.querySelector('.history-select-all span');
        if (selectAllLabel) selectAllLabel.textContent = t('selectAll');
        if (deleteSelectedButton) deleteSelectedButton.textContent = t('deleteSelected');
        if (deleteAllButton) deleteAllButton.textContent = t('deleteAll');

        if (historyPageBody) {
            renderHistoryPageTable();
        }
    }

    function formatHistoryDate(dateValue) {
        if (!dateValue) return '—';

        const date = new Date(dateValue);

        if (Number.isNaN(date.getTime())) return '—';

        return new Intl.DateTimeFormat(
            currentLanguage === 'ru' ? 'ru-RU' : 'en-US',
            {
                dateStyle: 'medium',
                timeStyle: 'short'
            }
        ).format(date);
    }

    function renderHistoryTable(transcriptions, targetBody, searchValue = '') {
        if (!targetBody) return;

        targetBody.replaceChildren();

        const query = searchValue.trim().toLowerCase();

        const filtered = transcriptions.filter(item => {
            const searchableText = [
                item.fileName,
                item.language,
                item.transcript,
                item.createdAt
            ].join(' ').toLowerCase();

            return searchableText.includes(query);
        });

        if (filtered.length === 0) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');

            cell.colSpan = 5;
            cell.className = 'empty-history';
            cell.textContent = query ? t('searching') : t('noHistory');

            row.appendChild(cell);
            targetBody.appendChild(row);
            return;
        }

        filtered.forEach(item => {
            const row = document.createElement('tr');

            const nameCell = document.createElement('td');
            nameCell.textContent = item.fileName || '—';

            const durationCell = document.createElement('td');
            durationCell.textContent = '—';

            const languageCell = document.createElement('td');

            languageCell.textContent = item.language === 'ru'
                ? t('languageRussian')
                : item.language === 'en'
                    ? t('languageEnglish')
                    : item.language || '—';

            const createdCell = document.createElement('td');
            createdCell.textContent = formatHistoryDate(item.createdAt);

            const statusCell = document.createElement('td');
            const status = document.createElement('span');

            status.className = 'status completed';
            status.textContent = t('completedStatus');

            statusCell.appendChild(status);

            row.append(
                nameCell,
                durationCell,
                languageCell,
                createdCell,
                statusCell
            );

            targetBody.appendChild(row);
        });
    }

    function getFilteredHistory() {
        const query = historySearchInput?.value.trim().toLowerCase() || '';

        return cachedHistory.filter(item => {
            const searchableText = [
                item.fileName,
                item.language,
                item.transcript,
                item.createdAt
            ].join(' ').toLowerCase();

            return searchableText.includes(query);
        });
    }

    function updateHistorySelection() {
        if (!historyPage) return;

        const visibleIds = getFilteredHistory()
            .filter(item => item.id !== null && item.id !== undefined)
            .map(item => String(item.id));

        const selectedVisibleCount = visibleIds
            .filter(id => selectedHistoryIds.has(id)).length;

        if (selectAllHistoryCheckbox) {
            selectAllHistoryCheckbox.checked =
                visibleIds.length > 0 && selectedVisibleCount === visibleIds.length;
            selectAllHistoryCheckbox.indeterminate =
                selectedVisibleCount > 0 && selectedVisibleCount < visibleIds.length;
            selectAllHistoryCheckbox.disabled = visibleIds.length === 0 || isDeletingHistory;
        }

        if (deleteSelectedButton) {
            deleteSelectedButton.disabled =
                selectedHistoryIds.size === 0 || isDeletingHistory;
        }

        if (deleteAllButton) {
            deleteAllButton.disabled = cachedHistory.length === 0 || isDeletingHistory;
        }

        historyPage.querySelectorAll('.history-record-checkbox, .history-delete-button')
            .forEach(control => {
                control.disabled = isDeletingHistory;
            });
    }

    function renderHistoryPageTable() {
        if (!historyPageBody) return;

        historyPageBody.replaceChildren();
        const filtered = getFilteredHistory();

        if (filtered.length === 0) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');

            cell.colSpan = 7;
            cell.className = 'empty-history';
            cell.textContent = historySearchInput?.value.trim()
                ? t('searching')
                : t('noHistory');

            row.appendChild(cell);
            historyPageBody.appendChild(row);
            updateHistorySelection();
            return;
        }

        filtered.forEach(item => {
            const row = document.createElement('tr');
            const id = item.id === null || item.id === undefined
                ? null
                : String(item.id);

            const selectionCell = document.createElement('td');
            if (id !== null) {
                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.className = 'history-record-checkbox';
                checkbox.dataset.id = id;
                checkbox.checked = selectedHistoryIds.has(id);
                checkbox.setAttribute('aria-label', `${t('selectAll')}: ${item.fileName || id}`);
                selectionCell.appendChild(checkbox);
            }

            const nameCell = document.createElement('td');
            nameCell.textContent = item.fileName || '—';

            const durationCell = document.createElement('td');
            durationCell.textContent = '—';

            const languageCell = document.createElement('td');
            languageCell.textContent = item.language === 'ru'
                ? t('languageRussian')
                : item.language === 'en'
                    ? t('languageEnglish')
                    : item.language || '—';

            const createdCell = document.createElement('td');
            createdCell.textContent = formatHistoryDate(item.createdAt);

            const statusCell = document.createElement('td');
            const status = document.createElement('span');
            status.className = 'status completed';
            status.textContent = t('completedStatus');
            statusCell.appendChild(status);

            const actionCell = document.createElement('td');
            if (id !== null) {
                const deleteButton = document.createElement('button');
                deleteButton.type = 'button';
                deleteButton.className = 'history-delete-button danger-button';
                deleteButton.dataset.id = id;
                deleteButton.textContent = t('deleteRecord');
                deleteButton.disabled = isDeletingHistory;
                actionCell.appendChild(deleteButton);
            }

            row.append(
                selectionCell,
                nameCell,
                durationCell,
                languageCell,
                createdCell,
                statusCell,
                actionCell
            );

            historyPageBody.appendChild(row);
        });

        updateHistorySelection();
    }

    function setHistoryActionStatus(message, isError = false) {
        if (!historyStatus) return;

        historyStatus.textContent = message;
        historyStatus.classList.toggle('is-error', isError);
    }

    async function sendHistoryDeleteRequest(url, body) {
        const options = {
            method: 'DELETE',
            headers: { 'Accept': 'application/json' }
        };

        if (body !== undefined) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(body);
        }

        const response = await fetch(url, options);
        if (!response.ok) {
            let message = '';
            try {
                const result = await response.json();
                message = result.message || result.error || '';
            } catch {
                // Сервер может вернуть пустой ответ.
            }
            throw new Error(message || `HTTP ${response.status}`);
        }
    }

    async function performHistoryDelete(url, body) {
        if (isDeletingHistory) return;

        isDeletingHistory = true;
        updateHistorySelection();
        setHistoryActionStatus('');

        try {
            await sendHistoryDeleteRequest(url, body);
            selectedHistoryIds.clear();
            await loadHistory();
            setHistoryActionStatus(t('historyDeleted'));
        } catch (error) {
            console.error('AUREVA history deletion error:', error);
            setHistoryActionStatus(`${t('deleteFailed')} ${error.message}`, true);
        } finally {
            isDeletingHistory = false;
            renderHistoryPageTable();
        }
    }

    async function deleteHistoryRecord(id) {
        if (!window.confirm(t('confirmDeleteRecord'))) return;

        selectedHistoryIds.delete(String(id));
        await performHistoryDelete(
            `${CONFIG.historyUrl}/${encodeURIComponent(id)}`
        );
    }

    async function deleteSelectedHistory() {
        const ids = Array.from(selectedHistoryIds, id => Number(id))
            .filter(Number.isSafeInteger);
        if (ids.length === 0) {
            setHistoryActionStatus(t('nothingSelected'), true);
            return;
        }

        if (!window.confirm(t('confirmDeleteSelected'))) return;
        await performHistoryDelete(`${CONFIG.historyUrl}/selected`, ids);
    }

    async function deleteAllHistory() {
        if (cachedHistory.length === 0) return;
        if (!window.confirm(t('confirmDeleteAll'))) return;

        await performHistoryDelete(CONFIG.historyUrl);
    }

    async function loadHistory() {
        try {
            const response = await fetch(CONFIG.historyUrl, {
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const transcriptions = await response.json();

            if (!Array.isArray(transcriptions)) {
                throw new Error('Invalid history response');
            }

            cachedHistory = transcriptions;

            const existingIds = new Set(
                cachedHistory
                    .filter(item => item.id !== null && item.id !== undefined)
                    .map(item => String(item.id))
            );

            selectedHistoryIds.forEach(id => {
                if (!existingIds.has(id)) selectedHistoryIds.delete(id);
            });

            renderHistoryTable(cachedHistory, elements.historyBody);
            renderHistoryPageTable();

        } catch (error) {
            console.error('AUREVA history error:', error);

            const targets = [elements.historyBody, historyPageBody];

            targets.forEach(target => {
                if (!target) return;

                target.replaceChildren();

                const row = document.createElement('tr');
                const cell = document.createElement('td');

                cell.colSpan = target === historyPageBody ? 7 : 5;
                cell.className = 'empty-history';
                cell.textContent = t('requestFailed');

                row.appendChild(cell);
                target.appendChild(row);
            });

            updateHistorySelection();
        }
    }

    // 19. RECENT FILES SEARCH

    function filterHistory(searchValue) {
        renderHistoryTable(cachedHistory, elements.historyBody, searchValue);
    }

    elements.fileSearch?.addEventListener('input', event => {
        filterHistory(event.target.value);
    });

    // 20. MAIN NAVIGATION

    function setActiveNavigation(activeElement) {
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });

        activeElement?.classList.add('active');
    }

    function showPage(page) {
        if (!historyPage) createHistoryPage();

        const isHistory = page === 'history';
        const isRecord = page === 'record';
        const isSettings = page === 'settings';

        if (elements.heroBanner) {
            elements.heroBanner.hidden = isHistory || isRecord || isSettings;
        }

        if (elements.workspace) {
            elements.workspace.hidden = isHistory || isSettings;
        }

        if (elements.resultsGrid) {
            elements.resultsGrid.hidden = isHistory || isRecord || isSettings;
        }

        if (elements.recentFiles) {
            elements.recentFiles.hidden = isHistory || isRecord || isSettings;
        }

        if (historyPage) {
            historyPage.hidden = !isHistory;
        }

        if (elements.settingsPage) {
            elements.settingsPage.hidden = !isSettings;
        }

        if (page === 'transcribe') {
            setActiveNavigation(elements.navTranscribe);
            showUploadTab();
        }

        if (page === 'record') {
            setActiveNavigation(elements.navRecord);
            showRecordingTab();
        }

        if (page === 'history') {
            setActiveNavigation(elements.navHistory);
            loadHistory();
        }

        if (page === 'settings') {
            setActiveNavigation(elements.navSettings);
            initializeSettings();
            showSettingsSaveStatus(true);
        }

        history.replaceState(null, '', `#${page}`);
    }

    elements.navTranscribe?.addEventListener('click', event => {
        event.preventDefault();
        showPage('transcribe');
    });

    elements.navRecord?.addEventListener('click', event => {
        event.preventDefault();
        showPage('record');
    });

    elements.navHistory?.addEventListener('click', event => {
        event.preventDefault();
        showPage('history');
    });

    elements.navSettings?.addEventListener('click', event => {
        event.preventDefault();
        showPage('settings');
    });

    document.getElementById('history-view-all')?.addEventListener('click', event => {
        event.preventDefault();
        showPage('history');
    });

    document.getElementById('brand-home')?.addEventListener('click', event => {
        event.preventDefault();
        showPage('transcribe');
    });

    // 21. PLACEHOLDER ACTIONS

    document.getElementById('upgrade-button')?.addEventListener('click', () => {
        showProcessingMessage(t('upgradeUnavailable'));
    });

    document.getElementById('notifications-button')?.addEventListener('click', () => {
        showProcessingMessage(t('noNotifications'));
    });

    document.getElementById('account-menu-button')?.addEventListener('click', () => {
        showProcessingMessage(t('accountUnavailable'));
    });

    document.getElementById('logout-link')?.addEventListener('click', event => {
        event.preventDefault();
        showProcessingMessage(t('authenticationUnavailable'));
    });

    document.getElementById('help-support-link')?.addEventListener('click', event => {
        event.preventDefault();
        showProcessingMessage(t('supportUnavailable'));
    });

    // 22. INITIALIZATION

    function initializeApplication() {
        applyTheme(currentTheme);
        applyLanguage(currentLanguage);

        initializeSettings();

        createHistoryPage();

        setTranscribeButtonState(true);

        if (elements.selectedFileName) {
            elements.selectedFileName.textContent = t('noFile');
        }

        if (elements.recordingPanel) {
            elements.recordingPanel.hidden = true;
        }

        if (elements.dropzone) {
            elements.dropzone.hidden = false;
        }

        if (elements.recordingPreview) {
            elements.recordingPreview.hidden = true;
        }

        if (elements.useRecordingButton) {
            elements.useRecordingButton.disabled = true;
        }

        resetTranscript();

        loadHistory();

        const initialPage = window.location.hash === '#history'
            ? 'history'
            : window.location.hash === '#record'
                ? 'record'
                : window.location.hash === '#settings'
                    ? 'settings'
                    : 'transcribe';

        showPage(initialPage);
    }

    initializeApplication();

    // 23. CLEANUP

    window.addEventListener('beforeunload', () => {
        if (recordingInterval) {
            clearInterval(recordingInterval);
        }

        releaseRecordingStream();
        clearRecordingObjectUrl();
    });
});