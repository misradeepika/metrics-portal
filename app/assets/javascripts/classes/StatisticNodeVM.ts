/*
 * Copyright 2014 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

///<reference path="../libs/knockout/knockout.d.ts" />
///<reference path="./BrowseNode.ts"/>
import GraphSpec = require('./GraphSpec');
import ko = require('knockout');

declare var require;
class StatisticNodeVM implements BrowseNode {
    serviceName: string;
    metricName: string;
    statisticName: string;
    children: KnockoutObservableArray<BrowseNode>;
    subfolders: KnockoutObservableArray<BrowseNode>;
    expanded: KnockoutObservable<boolean>;
    name: KnockoutObservable<string>;
    renderAs: KnockoutObservable<string>;
    icon: KnockoutComputed<string>;
    visible: KnockoutObservable<boolean>;

    expandMe: () => void;

    constructor(spec: GraphSpec, id: string) {
        this.serviceName = spec.service;
        this.metricName = spec.metric;
        this.statisticName = spec.statistic;
        this.children = ko.observableArray<BrowseNode>();
        this.subfolders = ko.observableArray<BrowseNode>();
        this.expanded = ko.observable(false);
        this.name = ko.observable(this.statisticName);
        this.renderAs = ko.observable("statistic_node");
        this.icon = ko.pureComputed<string>(() => { return this.cssIcon(); });
        this.visible = ko.observable(true);

        this.expandMe = () => {
            this.expanded(!this.expanded());
            if (this.expanded()) {
                require('./GraphViewModel').addGraph(spec);
            } else {
                require('./GraphViewModel').removeGraph(spec);
            }
        };
    }

    cssIcon(): string {
        return this.expanded() ? "fa fa-eye" : "";
    }
}

export = StatisticNodeVM;
