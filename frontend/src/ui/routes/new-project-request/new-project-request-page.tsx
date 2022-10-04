import {Classes} from '@blueprintjs/core';
import React, {FC, memo} from 'react';
import {ProjectRequest} from 'src/ui/blocks/default-form-view/project-request';
import {Layout} from 'src/ui/blocks/layout/layout';
import {Tab} from 'src/ui/blocks/tab/tab';

export const NewProjectRequestPage: FC = memo(() => {
    const leftComponent = (
        <div className={Classes.FILL}>
            {[{title: 'Подать заявку на проект', href: '/new_project_request'}].map(
                ({title, href}, index) => (
                    <Tab key={index} isActive={true} link={href} title={title} />
                )
            )}
        </div>
    );
    return <Layout rightComponent={<ProjectRequest />} leftComponent={leftComponent} />;
});
