export interface Arc {
    func: any;
    entity: any;
    global: any;
    createEntityService(): any;
    createListService(type: string): any;
}

export interface UserSender {
    send(requestingUserId: string, payload: object): void;
}

export interface ResponseSender {
    send(requestId: string, payload: object): void;
}

interface TransactionEntity {
    id: string;
    service: string;
    func: string;
}

interface Transaction {
    entities: TransactionEntity[];
}

export interface TransactionManager {
    startTransaction(transaction: Transaction, requestId: string, requestingUserId: string, payload: object): void;
}

export interface Tools {
    randomUUID(): string;
    createUUID(mostSigBits: number, leastSigBits: number): string;
    hashStringToUUID(str: string): string;
    isUUID(uuidString: string): boolean;
    randomNumber(origin: number, bound: number): number;
    println(obj: any): void;
    currentTimeMillis(): number;
    sleep(millis: number): void;
}

export interface EntityServices {
    send(service: string, func: string, entityId: string, requestId: string, requestingUserId: string, payload: object): void;
}

export interface ListServices {
    add(service: string, elemToAdd: any, entityId: string): void;
    remove(service: string, elemToRemove: any, entityId: string): void;
    collect(service: string, finalFunctionId: string, entityId: string, requestId: string, requestingUserId: string): void;
}

interface AggregationIndex {
    name: string,
    id: string
}

interface AggregationIndices {
    indices: AggregationIndex[]
}

interface AggregationService {
    start(
        entityServiceName: string,
        mappingFunctionId: string,
        finalFunctionId: string,
        aggregationIndicesJsJson: AggregationIndices,
        requestId: string,
        requestingUserId: string): void;
}

export interface CollectFinalMessage {
    list: any[];
}

export enum LogLevel {
    ERROR = 0,
    WARN = 1,
    INFO = 2,
    DEBUG = 3
}

interface Logger {
    log(logLevel: LogLevel, message: string): void;
}

export enum HttpMethod {
    GET = 0,
    POST = 1,
    PUT = 2,
    DELETE = 3
}

export interface HttpHeader {
    key: string;
    value: string;
}

export interface HttpHeaders {
    headers: HttpHeader[];
}

export interface HttpClientResponse {
    payload: any;
    httpResponse: {
        code: number;
        body: string;
    };
}

export interface HttpClient {
    request(
        url: string,
        method: HttpMethod,
        body: string,
        headers: HttpHeaders,
        resultFunc: string,
        requestId: string,
        requestingUserId: string,
        payload: object): void;
}

export enum EntityAction {
    NOOP = 0,
    STORE = 1,
    REMOVE = 2
}

export interface RequestInfo {
    ip: string;
}

export let arc: Arc;
export let responseSender: ResponseSender;
export let userSender: UserSender;
export let services: EntityServices;
export let listServices: ListServices;
export let transactionManager: TransactionManager;
export let tools: Tools;
export let aggregationService: AggregationService;
export let log: Logger;
export let httpClient: HttpClient;